@echo off
chcp 65001 > nul
setlocal

:: ==========================================
::   Continue ARS Simulator Launcher
:: ==========================================

:: 설정: AWS 서버 IP (필요시 수정)
set SERVER_IP=54.250.229.56
set BASE_URL=http://%SERVER_IP%/api/v1/ars

:: 경로 설정 (프로젝트 구조에 맞게 이동)
set SCRIPT_DIR=%~dp0
set JAVA_DIR=%SCRIPT_DIR%trustee-callcenter\backend\src\main\java

echo ========================================================
echo   [Continue Bank] ARS Simulator
echo   Target Server: %SERVER_IP%
echo ========================================================
echo.

:: Java 소스 폴더로 이동
cd /d "%JAVA_DIR%"

echo [1/2] Compiling...
javac -encoding UTF-8 com/callcenter/callcenterwas/ArsSimulator.java
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] 컴파일 실패! Java가 설치되어 있는지 확인해주세요.
    pause
    exit /b
)

echo [2/2] Starting System...
echo.
:: 실행
java -Dars.api.base-url=%BASE_URL% com.callcenter.callcenterwas.ArsSimulator

echo.
echo [INFO] Program terminated.
pause
