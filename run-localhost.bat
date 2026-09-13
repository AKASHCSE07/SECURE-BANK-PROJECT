@echo off
REM ============================================================
REM  SecureBank — Instant Localhost Server
REM  Runs the frontend web application on http://localhost:3000
REM ============================================================

title SecureBank - Localhost Server (Port 3000)
echo ============================================================
echo   SecureBank Localhost Server
echo ============================================================
echo.

REM Check Java is available
java --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not found on PATH.
    echo Please install Java JDK 17 and try again.
    pause
    exit /b 1
)

echo [OK] Java detected.
echo Starting web server on http://localhost:3000 ...
echo.
echo Opening browser...
start http://localhost:3000/index.html
echo.
echo ============================================================
echo   Web Server is LIVE at:
echo   - Home Page:           http://localhost:3000/index.html
echo   - Customer Dashboard:  http://localhost:3000/customer-dashboard.html
echo   - Admin Dashboard:     http://localhost:3000/admin-dashboard.html
echo   - Login:               http://localhost:3000/login.html
echo ============================================================
echo   Keep this window OPEN while using the application.
echo   Press Ctrl+C or close this window to stop the server.
echo ============================================================
echo.

java LocalServer.java
pause
