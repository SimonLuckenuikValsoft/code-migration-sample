@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo Order Entry Application - Automated Setup
echo ==========================================
echo.

cd /d "%~dp0"

java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo Java found:
    java -version
) else (
    echo Java not found. Please install OpenJDK 11+ from:
    echo https://adoptium.net/
    echo.
    echo Or install using winget:
    echo   winget install EclipseAdoptium.Temurin.11.JDK
    echo.
    echo After installing Java, run this script again.
    pause
    exit /b 1
)

echo.
echo Downloading dependencies and building application...
echo.

call mvnw.cmd clean compile -q

if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo ==========================================
echo Setup Complete!
echo ==========================================
echo.
echo Starting application...
echo.

call mvnw.cmd -q exec:java
