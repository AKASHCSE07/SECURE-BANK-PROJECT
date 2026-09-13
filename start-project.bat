@echo off
REM ============================================================
REM  SecureBank — Master Project Launcher
REM  Starts Backend & Localhost Web Server in synchronized windows.
REM ============================================================

title SecureBank - Project Launcher
echo ============================================================
echo   SecureBank Enterprise Banking Platform
echo ============================================================
echo.

REM --- Check Java is available ---
java --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java 17+ is not found on PATH.
    echo Please install Java JDK 17 and ensure JAVA_HOME is set.
    pause
    exit /b 1
)

REM --- Check Maven is available ---
mvn --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven is not found on PATH.
    echo Please install Apache Maven 3.8+ and ensure bin is on PATH.
    pause
    exit /b 1
)

echo [OK] Java 17 and Maven detected.
echo.
echo [1/2] Starting Frontend Localhost Web Server (Port 3000)...
start "SecureBank - Frontend Server (Port 3000)" cmd /k "cd /d %~dp0 && java LocalServer.java"

echo [2/2] Starting Spring Boot REST Backend (Port 8080)...
start "SecureBank - Spring Boot API (Port 8080)" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo.
echo Opening browser...
timeout /t 2 >nul
start http://localhost:3000/index.html

echo ============================================================
echo   SecureBank is LIVE at:
echo   -> Web Application:     http://localhost:3000/index.html
echo   -> Customer Dashboard:  http://localhost:3000/customer-dashboard.html
echo   -> Admin Suite:         http://localhost:3000/admin-dashboard.html
echo   -> Spring Boot API:     http://localhost:8080/api/auth/login
echo ============================================================
echo.
pause
