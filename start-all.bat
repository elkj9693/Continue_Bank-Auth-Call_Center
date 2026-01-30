@echo off
echo.
echo ==========================================
echo   Continue Bank System Start
echo ==========================================
echo.

REM Check Docker
docker ps >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not running!
    echo Please start Docker Desktop.
    pause
    exit /b 1
)

REM 1. Start Databases
echo [1/7] Starting Databases...
docker-compose up -d mysql-entrusting mysql-trustee mysql-callcenter
echo.

REM 2. Bank Backend
echo [2/7] Starting Bank Backend...
start "Bank-Backend" cmd /k "cd /d "%~dp0entrusting-client\backend" && mvn spring-boot:run"

REM 3. Auth Backend
echo [3/7] Starting Auth Backend...
start "Auth-Backend" cmd /k "cd /d "%~dp0trustee-provider\backend" && mvn spring-boot:run"

REM 4. CallCenter Backend
echo [4/7] Starting CallCenter Backend...
start "CallCenter-Backend" cmd /k "cd /d "%~dp0trustee-callcenter\backend" && gradlew bootRun"

REM 5. Bank Frontend
echo [5/7] Starting Bank Frontend...
start "Bank-Web" cmd /k "cd /d "%~dp0entrusting-client\frontend" && npm run dev"

REM 6. Auth Frontend
echo [6/7] Starting Auth Frontend...
start "Auth-Web" cmd /k "cd /d "%~dp0trustee-provider\frontend" && npm run dev"

REM 7. CallCenter Frontend
echo [7/7] Starting CallCenter Frontend...
start "CallCenter-Web" cmd /k "cd /d "%~dp0trustee-callcenter\frontend" && npm run dev"

echo.
echo ==========================================
echo   System Start Sequence Completed!
echo ==========================================
echo.
echo Bank:      http://localhost:5175
echo Auth:      http://localhost:5176
echo CallCenter: http://localhost:5178
echo.
pause
