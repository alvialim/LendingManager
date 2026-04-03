@echo off
REM Haftabook — build Windows installer (.exe) for the desktop app.
REM Requirements: Windows 10/11, JDK 17+ (JAVA_HOME), Android Studio / Gradle uses project wrapper.
REM Output folder (after success): composeApp\build\compose\binaries\main-release\exe\
cd /d "%~dp0"
echo Building Windows .exe (Compose Desktop / jpackage)...
call gradlew.bat :composeApp:packageExe
if errorlevel 1 (
  echo Build failed. If the task was not found, try: gradlew.bat :composeApp:tasks --group=compose
  exit /b 1
)
echo.
echo Done. Copy the .exe from:
echo   composeApp\build\compose\binaries\
echo.
pause
