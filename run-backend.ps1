# Script to start backend services in separate windows
# Note: Ensure MySQL, Kafka, and Redis are running before starting this.

function Start-ServiceWindow {
    param($Name, $Path)
    Write-Host "Starting $Name..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $Path; mvn spring-boot:run" -WindowStyle Normal
}

# 1. Eureka Server (Wait for it to be up)
Start-ServiceWindow "Eureka Server" "eureka-server"
Write-Host "Waiting 15s for Eureka..."
Start-Sleep -Seconds 15

# 2. API Gateway
Start-ServiceWindow "API Gateway" "api-gateway"

# 3. Core Services
Start-ServiceWindow "Auth Service" "auth-service"
Start-ServiceWindow "User Service" "user-service"
Start-ServiceWindow "Account Service" "account-service"
Start-ServiceWindow "Transaction Service" "transaction-service"
Start-ServiceWindow "Loan Service" "loan-service"
Start-ServiceWindow "Notification Service" "notification-service"
Start-ServiceWindow "Admin Service" "admin-service"

Write-Host "`nAll services are starting in separate windows." -ForegroundColor Green
Write-Host "You can monitor them individually."
