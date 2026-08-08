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

# Clean build target folders
Write-Host "`nCleaning up build target directories..." -ForegroundColor Yellow
$serviceDirs = @("eureka-server", "api-gateway", "auth-service", "user-service", "account-service", "transaction-service", "loan-service", "notification-service", "admin-service")
foreach ($dir in $serviceDirs) {
    $targetPath = Join-Path $PSScriptRoot "$dir\target"
    if (Test-Path $targetPath) {
        Remove-Item -Path $targetPath -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "Cleaned $dir\target" -ForegroundColor Cyan
    }
}
Write-Host "Workspace is now extra clean!" -ForegroundColor Green
