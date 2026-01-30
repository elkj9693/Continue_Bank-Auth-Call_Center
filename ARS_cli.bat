@echo off
chcp 65001 > nul
echo.
echo ==========================================
echo   Continue Card ARS Simulator
echo ==========================================
echo.
echo [INFO] Running ARS Simulator...
echo.

java -Dfile.encoding=UTF-8 "trustee-callcenter\backend\src\main\java\com\callcenter\callcenterwas\ArsSimulator.java"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Execution failed.
    echo Please check if Java is installed and the path is correct.
)

pause
