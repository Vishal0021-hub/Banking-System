# Script to start backend services in separate windows
# Note: Ensure MySQL, Kafka, and Redis are running before starting this.

# Load environment variables from .env if it exists
if (Test-Path ".env") {
    Write-Host "Loading environment variables from .env..." -ForegroundColor Green
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $key, $value = $line -split '=', 2
            if ($key -and $value) {
                $key = $key.Trim()
                $value = $value.Trim()
                # Remove quotes if present
                $value = $value.Trim("`"").Trim("'")
                [System.Environment]::SetEnvironmentVariable($key, $value, [System.EnvironmentVariableTarget]::Process)
            }
        }
    }
}

function Start-ServiceWindow {
    param($Name, $Path)
    Write-Host "Starting $Name..." -ForegroundColor Cyan
    $absPath = Resolve-Path $Path
    $cmd = "try { `$Host.UI.RawUI.WindowTitle = 'Banking-Service-$Name' } catch {}; . ../load-env.ps1; mvn spring-boot:run"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd -WorkingDirectory $absPath -WindowStyle Normal
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
