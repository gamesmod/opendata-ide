<#
.SYNOPSIS
  Smoke/integration тесты (ТЗ, раздел 36). С -WithPostgres поднимает тестовую PostgreSQL
  из docker/docker-compose.yml и прогоняет JDBC-тесты.
.EXAMPLE
  .\scripts\test.ps1 -WithPostgres
#>
param(
    [string]$IdeHome,
    [switch]$WithPostgres
)
. "$PSScriptRoot\common.ps1"

$java = Assert-Jdk
$ide = Find-JetBrainsIde $IdeHome
$null = Invoke-Research -Java $java -IdeHome $ide -CheckOnly
$null = Assert-Compatibility $ide

if ($WithPostgres) {
    Write-Step 'PostgreSQL (docker compose)'
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { Fail 'docker не найден. Установите Docker Desktop или задайте OPENDATA_PG_URL на свою БД.' }
    & docker compose -f (Join-Path $script:ProjectRoot 'docker/docker-compose.yml') up -d --wait | Out-Host
    if ($LASTEXITCODE -ne 0) { Fail 'Не удалось запустить PostgreSQL' }
    if (-not $env:OPENDATA_PG_URL) { $env:OPENDATA_PG_URL = 'jdbc:postgresql://localhost:54329/opendata' }
}
if (-not $env:OPENDATA_PG_URL) { Write-Host 'OPENDATA_PG_URL не задан — JDBC-тесты будут пропущены' -ForegroundColor Yellow }

$gradleArgs = @("-PlocalIdePath=$ide", '--console=plain', ':opendata:integration:test')
$jbr = Get-IdeJbrJava $ide
if ($jbr) { $gradleArgs += "-Porg.gradle.java.installations.paths=$(Split-Path (Split-Path $jbr -Parent) -Parent)" }
Invoke-Gradle $gradleArgs
Write-Host 'TESTS OK' -ForegroundColor Green
Write-Host 'Отчёт: opendata/integration/build/reports/tests/test/index.html'
