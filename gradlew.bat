@echo off
setlocal
set "GRADLE_VERSION=9.5.0"
set "ROOT=%~dp0"
set "DIST_ROOT=%ROOT%.gradle-dist"
set "GRADLE_HOME=%DIST_ROOT%\gradle-%GRADLE_VERSION%"
set "ZIP_FILE=%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip"
set "URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
  if not exist "%ZIP_FILE%" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing '%URL%' -OutFile '%ZIP_FILE%'"
    if errorlevel 1 exit /b 1
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP_FILE%' '%DIST_ROOT%'"
  if errorlevel 1 exit /b 1
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
