# Version Windows (PowerShell) de build.sh
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
Write-Host "==> Build Angular (production)"
Push-Location frontend; npm install; npx ng build; Pop-Location
Write-Host "==> Build Spring Boot + tests"
Push-Location backend; mvn clean package; Pop-Location
Write-Host "==> OK : backend\target\taskflow.jar"
