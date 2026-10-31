# Script to gracefully stop all running Java backend microservices

Write-Host "Stopping all Spring Boot Microservices..." -ForegroundColor Yellow

# Find all Java processes and stop them
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Stop-Process -Name java -Force
    Write-Host "All backend services have been stopped successfully!" -ForegroundColor Green
} else {
    Write-Host "No backend services were running." -ForegroundColor Cyan
}

# Close spawned PowerShell windows
Write-Host "Closing terminal windows..." -ForegroundColor Yellow
$serviceTerminals = Get-Process powershell -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like "Banking-Service-*" }
if ($serviceTerminals) {
    $serviceTerminals | Stop-Process -Force
    Write-Host "Closed $($serviceTerminals.Count) microservice terminal windows!" -ForegroundColor Green
} else {
    Write-Host "No open microservice terminal windows found." -ForegroundColor Cyan
}

# Clean build target folders recursively
Write-Host "`nCleaning up build target directories..." -ForegroundColor Yellow
Get-ChildItem -Path $PSScriptRoot -Recurse -Directory -Filter "target" -ErrorAction SilentlyContinue | ForEach-Object {
    $relativePath = $_.FullName.Substring($PSScriptRoot.Length + 1)
    Remove-Item -Path $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "Cleaned: $relativePath" -ForegroundColor Cyan
}
Write-Host "Workspace is now extra clean!" -ForegroundColor Green
