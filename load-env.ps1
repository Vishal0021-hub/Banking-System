# Helper script to load environment variables from .env into the current PowerShell process environment
# This should be dot-sourced (e.g. `. ../load-env.ps1`) so variables persist in the calling session.

$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $key, $value = $line -split '=', 2
            if ($key -and $value) {
                $key = $key.Trim()
                $value = $value.Trim().Trim('"').Trim("'")
                Set-Item -Path "env:$key" -Value $value
            }
        }
    }
}

$mavenBin = "C:\Users\mishr\Downloads\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin"
if ((Test-Path $mavenBin) -and ($env:PATH -notlike "*$mavenBin*")) {
    $env:PATH = "$mavenBin;" + $env:PATH
}
