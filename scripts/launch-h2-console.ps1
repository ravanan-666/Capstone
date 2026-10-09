# DJ Mart - H2 Database Console Launcher
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "          DJ Mart - H2 Database Console" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""

$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

$CandidateJars = @(
    (Join-Path $ProjectRoot "target\djmart\WEB-INF\lib\h2-2.2.224.jar"),
    (Join-Path $HOME ".m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"),
    "C:\Program Files (x86)\H2\bin\h2-2.5.252.jar"
)

$H2Jar = $null
foreach ($jar in $CandidateJars) {
    if (Test-Path $jar) {
        $H2Jar = $jar
        break
    }
}

if (-not $H2Jar) {
    Write-Host "[ERROR] H2 JAR could not be found." -ForegroundColor Red
    Write-Host "Please run 'mvn clean package -DskipTests' first to download dependencies." -ForegroundColor Yellow
    exit 1
}

Write-Host "Using H2 JAR: $H2Jar" -ForegroundColor DarkGray
Write-Host ""
Write-Host "---------------------------------------------------" -ForegroundColor Green
Write-Host "Connection Settings for H2 Console:" -ForegroundColor Green
Write-Host "  JDBC URL : jdbc:h2:./data/djmart" -ForegroundColor White
Write-Host "  User Name: sa" -ForegroundColor White
Write-Host "  Password : (leave empty)" -ForegroundColor White
Write-Host "---------------------------------------------------" -ForegroundColor Green
Write-Host ""
Write-Host "Launching H2 Web Console in default browser at http://localhost:8082 ..." -ForegroundColor Cyan
Write-Host ""

& java -cp $H2Jar org.h2.tools.Console -web -browser -webPort 8082
