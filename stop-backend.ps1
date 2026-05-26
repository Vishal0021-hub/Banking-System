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
