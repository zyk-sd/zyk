@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ============================================
echo   Game Companion Platform - Starting...
echo ============================================

if exist "target\game-companion-platform-1.0.0.jar" goto runjar

echo Jar not found, running with Maven...
start "GameCompanionPlatform" cmd /c "mvn spring-boot:run"
timeout /t 15 /nobreak >nul
start "" http://localhost:8080
goto end

:runjar
start "GameCompanionPlatform" cmd /c "java -jar target\game-companion-platform-1.0.0.jar"
timeout /t 15 /nobreak >nul
start "" http://localhost:8080

:end
echo.
echo Browser should open http://localhost:8080
echo Demo accounts: admin / player01 / companion01 , password 123456
echo.
pause
