@echo off
REM ============================================================
REM  SecureBank — Core DSA & OOP Prototype Showcase Runner
REM  Compiles and executes the standalone Java DSA demonstrations.
REM ============================================================

title SecureBank - DSA & OOP Showcase
echo ============================================================
echo   SecureBank Core DSA and OOP Algorithms Engine
echo ============================================================
echo.

REM Check Java
java --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java 17+ is required.
    pause
    exit /b 1
)

echo [1/2] Compiling standalone Java classes...
if not exist "bin" mkdir bin
javac -d bin src\com\securebank\prototype\*.java src\com\securebank\dsa\*.java src\com\securebank\exception\*.java src\com\securebank\validator\*.java src\com\securebank\io\*.java

if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
echo [2/2] Launching DSA Showcase (HashMap, PriorityQueue, MergeSort, Binary Search)...
echo ============================================================
java -cp bin com.securebank.dsa.DSAMain
echo ============================================================
echo.
pause
