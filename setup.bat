@echo off
REM Setup script for Legacy Order Entry Application (Windows)
REM This script helps install everything required to build and run the Java app

echo ==========================================
echo Legacy Order Entry Application Setup
echo ==========================================
echo.

REM Check if Java is already installed
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo Java is already installed:
    java -version
    echo.
    echo Setup complete! You can now run:
    echo   mvnw.cmd -q exec:java              # Run the desktop application
    echo   mvnw.cmd compile                   # Compile the code
    echo.
    goto :end
)

echo Java is not installed.
echo.
echo To install Java on Windows:
echo.
echo 1. Download OpenJDK 11 or later from: https://adoptium.net/
echo    - Choose the Windows installer (.msi)
echo    - Select "Add to PATH" during installation
echo.
echo 2. After installation, restart this command prompt and run this script again
echo.
echo Alternative: If you have Chocolatey package manager installed, run:
echo   choco install openjdk11
echo.
echo Alternative: If you have winget installed (Windows 10+), run:
echo   winget install EclipseAdoptium.Temurin.11.JDK
echo.
pause
goto :end

:end
