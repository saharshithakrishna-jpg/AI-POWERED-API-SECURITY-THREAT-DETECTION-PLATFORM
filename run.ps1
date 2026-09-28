# Run from ANYWHERE in VS Code terminal:
#   .\run.ps1
# Or from any PowerShell prompt:
#   & "d:\AI-POWERED API SECURITY & THREAT DETECTION PLATFORM\api-security-platform\run.ps1"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = $scriptDir + "\"

Write-Host ""
Write-Host "  =========================================================" -ForegroundColor Cyan
Write-Host "   API Security & Threat Detection Platform - Starting..." -ForegroundColor Cyan
Write-Host "  =========================================================" -ForegroundColor Cyan
Write-Host ""

# Check MySQL
Write-Host "  [0/3] Checking MySQL on port 3306..." -NoNewline
try {
    $tcp = New-Object System.Net.Sockets.TcpClient("localhost", 3306)
    $tcp.Close()
    Write-Host " RUNNING" -ForegroundColor Green
} catch {
    Write-Host " NOT RUNNING" -ForegroundColor Red
    Write-Host ""
    Write-Host "  MySQL is not running! Start it first:" -ForegroundColor Yellow
    Write-Host "    > Win+R -> services.msc -> find MySQL80 -> Start" -ForegroundColor Yellow
    Write-Host "    > OR: net start MySQL80  (in Admin PowerShell)" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "  Press Enter to exit"
    exit 1
}

# Start ML Service
Write-Host "  [1/3] Starting ML Service      (Python FastAPI  -> port 8000)" -ForegroundColor White
Start-Process "cmd" -WorkingDirectory "$($root)ml-service" -ArgumentList "/k python train_model.py & uvicorn main:app --port 8000 --reload" -WindowStyle Normal

# Start Spring Boot Backend
Write-Host "  [2/3] Starting Backend         (Spring Boot     -> port 8080)" -ForegroundColor White
Start-Process "cmd" -WorkingDirectory "$($root)backend" -ArgumentList "/k mvn spring-boot:run" -WindowStyle Normal

# Start React Frontend
Write-Host "  [3/3] Starting Frontend        (React / Vite    -> port 5173)" -ForegroundColor White
Start-Process "cmd" -WorkingDirectory "$($root)frontend" -ArgumentList "/k node ./node_modules/vite/bin/vite.js" -WindowStyle Normal

Write-Host ""
Write-Host "  =========================================================" -ForegroundColor Cyan
Write-Host "   All 3 services launched in separate windows!" -ForegroundColor Green
Write-Host ""
Write-Host "   Wait ~30 seconds for Spring Boot, then open:" -ForegroundColor White
Write-Host "   --> http://localhost:5173" -ForegroundColor Yellow
Write-Host ""
Write-Host "   Login: admin / admin123" -ForegroundColor White
Write-Host "  =========================================================" -ForegroundColor Cyan
Write-Host ""
