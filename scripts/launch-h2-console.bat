@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo           DJ Mart - H2 Database Console
echo ===================================================
echo.

cd /d "%~dp0.."

set "H2_JAR=target\djmart\WEB-INF\lib\h2-2.2.224.jar"
if not exist "!H2_JAR!" (
    set "H2_JAR=%USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
)
if not exist "!H2_JAR!" (
    if exist "C:\Program Files (x86)\H2\bin\h2-2.5.252.jar" (
        set "H2_JAR=C:\Program Files (x86)\H2\bin\h2-2.5.252.jar"
    )
)

if not exist "!H2_JAR!" (
    echo [ERROR] H2 JAR could not be found.
    echo Please run 'mvn clean package -DskipTests' first.
    pause
    exit /b 1
)

echo Using H2 JAR: !H2_JAR!
echo.
echo ---------------------------------------------------
echo Connection Settings for H2 Console:
echo   JDBC URL : jdbc:h2:./data/djmart
echo   User Name: sa
echo   Password : (leave empty)
echo ---------------------------------------------------
echo.
echo Launching H2 Web Console in default browser...
echo.

java -cp "!H2_JAR!" org.h2.tools.Console -web -browser -webPort 8082

pause
