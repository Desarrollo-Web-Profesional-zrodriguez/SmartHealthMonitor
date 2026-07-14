Write-Host ">>> Starting all emulators..." -ForegroundColor Cyan
& .\start-emulators.ps1

Write-Host ">>> Deploying all applications..." -ForegroundColor Cyan
& .\deploy-apps.ps1
