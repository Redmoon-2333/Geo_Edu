@echo off
REM ============================================
REM GeoEdu Backend - Start Script (Windows)
REM ============================================

setlocal EnableDelayedExpansion

set APP_NAME=GeoEdu
set JAR_FILE=geoedu-0.0.1-SNAPSHOT.jar
set PID_FILE=geoedu.pid
set LOG_DIR=logs
set LOG_FILE=%LOG_DIR%\geoedu.log

if "%JAVA_OPTS%"=="" set JAVA_OPTS=-Xms512m -Xmx1024m

:main
if "%1"=="" goto start
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="status" goto status
goto usage

:check_java
echo [INFO] Checking Java version...
java -version 2>&1 | findstr /R "version" > nul
if errorlevel 1 (
    echo [ERROR] Java not found. Please install Java 17 or later.
    exit /b 1
)

for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%v
)
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "tokens=1 delims=." %%a in ("%JAVA_VERSION%") do set JAVA_MAJOR=%%a

if %JAVA_MAJOR% LSS 17 (
    echo [ERROR] Java version must be 17 or later. Current version: %JAVA_VERSION%
    exit /b 1
)
echo [INFO] Java version check passed (version %JAVA_MAJOR%)
exit /b 0

:check_port
set PORT=%SERVER_PORT%
if "%PORT%"=="" set PORT=8080
echo [INFO] Checking port %PORT%...
netstat -ano | findstr ":%PORT%" | findstr "LISTENING" > nul
if not errorlevel 1 (
    echo [WARN] Port %PORT% is already in use
    netstat -ano | findstr ":%PORT%" | findstr "LISTENING"
    choice /C YN /M "Do you want to continue?"
    if errorlevel 2 exit /b 1
) else (
    echo [INFO] Port %PORT% is available
)
exit /b 0

:start
echo [INFO] Starting %APP_NAME%...

call :check_java
if errorlevel 1 exit /b 1

call :check_port
if errorlevel 1 exit /b 1

if not exist "target\%JAR_FILE%" (
    echo [INFO] JAR file not found, building...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo [ERROR] Build failed
        exit /b 1
    )
)

if not exist "%LOG_DIR%" mkdir %LOG_DIR%

echo [INFO] JAVA_OPTS: %JAVA_OPTS%
echo [INFO] Starting application...

start "GeoEdu Backend" /min java %JAVA_OPTS% -jar target\%JAR_FILE%

timeout /t 5 /nobreak > nul

echo [INFO] %APP_NAME% started
echo [INFO] Logs: %LOG_FILE%
echo [INFO] Health check: http://localhost:%PORT%/api/v1/admin/health
goto end

:stop
echo [INFO] Stopping %APP_NAME%...

for /f "tokens=2" %%p in ('tasklist ^| findstr /i "java"') do (
    wmic process where "processid=%%p and commandline like '%%geoedu%%'" get processid 2>nul | findstr "%%p" >nul
    if not errorlevel 1 (
        echo [INFO] Killing process %%p
        taskkill /PID %%p /F > nul 2>&1
    )
)

echo [INFO] %APP_NAME% stopped
goto end

:restart
call :stop
timeout /t 2 /nobreak > nul
call :start
goto end

:status
echo [INFO] Checking %APP_NAME% status...

for /f "tokens=2" %%p in ('tasklist ^| findstr /i "java"') do (
    wmic process where "processid=%%p and commandline like '%%geoedu%%'" get processid 2>nul | findstr "%%p" >nul
    if not errorlevel 1 (
        echo [INFO] %APP_NAME% is running (PID: %%p)
        exit /b 0
    )
)
echo [INFO] %APP_NAME% is not running
exit /b 1

:usage
echo Usage: %0 {start^|stop^|restart^|status}
exit /b 1

:end
endlocal
