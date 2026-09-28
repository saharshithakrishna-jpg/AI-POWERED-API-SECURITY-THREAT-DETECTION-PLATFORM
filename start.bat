@echo off
setlocal

:: Get absolute path of this script's directory
set "ROOT=%~dp0"

echo.
echo  =========================================================
echo   AI-Powered API Security ^& Threat Detection Platform
echo  =========================================================
echo.

:: ── STEP 0: Check if MySQL (port 3306) is reachable ───────────────────────
echo  [0/3] Checking MySQL on port 3306...
powershell -Command "try { $t = New-Object System.Net.Sockets.TcpClient('localhost', 3306); $t.Close(); Write-Host '         MySQL is running. OK'; exit 0 } catch { Write-Host '  [WARN] MySQL is NOT running on port 3306!'; Write-Host '         Start MySQL first, then re-run this script.'; exit 1 }"
if %errorlevel% NEQ 0 (
    echo.
    echo  HOW TO START MYSQL:
    echo    Option A: Search "MySQL Workbench" in Start menu, open it - it starts MySQL.
    echo    Option B: Press Win+R, type "services.msc", find MySQL80, right-click Start.
    echo    Option C: Run this in PowerShell as Admin: net start MySQL80
    echo.
    echo  Once MySQL is running, run this script again.
    echo.
    pause
    exit /b 1
)
echo.

:: ── STEP 1: ML Service ────────────────────────────────────────────────────
echo  [1/3] Starting ML Service      (Python FastAPI  - port 8000)
echo        Installing Python dependencies first...
start "ML Service - port 8000" /D "%ROOT%ml-service" cmd /k "pip install -r requirements.txt && python train_model.py && uvicorn main:app --port 8000 --reload"

:: ── STEP 2: Spring Boot Backend ───────────────────────────────────────────
echo  [2/3] Starting Backend         (Spring Boot Java - port 8080)
echo        Using Maven Wrapper (mvnw.cmd) - no Maven install required
start "Backend - port 8080" /D "%ROOT%backend" cmd /k "mvnw.cmd spring-boot:run"

:: ── STEP 3: React Frontend ────────────────────────────────────────────────
echo  [3/3] Starting Frontend        (React / Vite    - port 5173)
start "Frontend - port 5173" /D "%ROOT%frontend" cmd /k "node ./node_modules/vite/bin/vite.js"

echo.
echo  =========================================================
echo   All 3 services launched in separate windows!
echo.
echo   Wait ~60 seconds for Spring Boot to finish starting.
echo   Then open:   http://localhost:5173
echo   Login with:  admin / admin123
echo.
echo   Ports:
echo     Frontend  ->  http://localhost:5173
echo     Backend   ->  http://localhost:8080
echo     ML Service->  http://localhost:8000
echo  =========================================================
echo.

endlocal

