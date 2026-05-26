# Verification script for Enterprise Banking System

Write-Host "--- Checking Environment ---" -ForegroundColor Cyan

# Check Java
try {
    $javaVer = java -version 2>&1 | Select-String "version"
    Write-Host "[OK] Java: $javaVer" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Java 21 not found" -ForegroundColor Red
}

# Check Node
try {
    $nodeVer = node -v
    Write-Host "[OK] Node: $nodeVer" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Node.js not found" -ForegroundColor Red
}

# Check Maven
try {
    $mvnVer = mvn -v | Select-Object -First 1
    Write-Host "[OK] Maven: $mvnVer" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Maven not found" -ForegroundColor Red
}

# Check MySQL (Optional check)
try {
    $mysqlVer = mysql --version
    Write-Host "[OK] MySQL: $mysqlVer" -ForegroundColor Green
} catch {
    Write-Host "[WARN] MySQL client not found in PATH" -ForegroundColor Yellow
}

Write-Host "`n--- Project Status ---" -ForegroundColor Cyan
$jarCount = (Get-ChildItem -Recurse -Filter "*.jar" | Where-Object { $_.DirectoryName -match "target" }).Count
Write-Host "Built JARs found: $jarCount"

Write-Host "`nReady to launch!" -ForegroundColor Cyan
