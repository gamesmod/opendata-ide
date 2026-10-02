<#
.SYNOPSIS
  Тесты DB-слоя (ТЗ, раздел 36) на реальных СУБД. С -WithDocker поднимает стенд docker\docker-compose.yml
  (PostgreSQL, ClickHouse, Dremio) и задаёт переменные OPENDATA_*_URL; без СУБД соответствующие тесты пропускаются.
.EXAMPLE
  .\scripts\test.ps1 -WithDocker
#>
param(
    [switch]$WithDocker,
    [string]$PlatformHome = $env:OPENDATA_PLATFORM_HOME
)
. "$PSScriptRoot\common.ps1"

$null = Assert-Jdk
$platform = Get-OssPlatform -Os 'windows' -Explicit $PlatformHome

if ($WithDocker) {
    Write-Step 'Тестовый стенд (docker compose)'
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { Fail 'docker не найден. Установите Docker Desktop или задайте OPENDATA_*_URL на свои СУБД.' }
    & docker compose -f (Join-Path $script:ProjectRoot 'docker/docker-compose.yml') up -d --wait | Out-Host
    if ($LASTEXITCODE -ne 0) { Fail 'Не удалось поднять стенд' }

    # Dremio: первый пользователь, пространство opendata, представление events_v (аналог docker/dremio/init.sh).
    $u = 'http://localhost:9047'
    try {
        Invoke-RestMethod -Method Put -Uri "$u/apiv2/bootstrap/firstuser" -Headers @{ Authorization = '_dremionull' } -ContentType 'application/json' `
            -Body '{"userName":"dremio","firstName":"Open","lastName":"Data","email":"dremio@example.com","password":"dremio123"}' | Out-Null
    } catch { }
    $token = (Invoke-RestMethod -Method Post -Uri "$u/apiv2/login" -ContentType 'application/json' -Body '{"userName":"dremio","password":"dremio123"}').token
    $h = @{ Authorization = "_dremio$token" }
    try { Invoke-RestMethod -Method Post -Uri "$u/api/v3/catalog" -Headers $h -ContentType 'application/json' -Body '{"entityType":"space","name":"opendata"}' | Out-Null } catch { }
    $sql = @{ sql = "CREATE OR REPLACE VIEW opendata.events_v AS SELECT * FROM (VALUES (1,'alpha'),(2,'beta'),(3,'gamma')) AS t(id, name)" } | ConvertTo-Json
    Invoke-RestMethod -Method Post -Uri "$u/api/v3/sql" -Headers $h -ContentType 'application/json' -Body $sql | Out-Null
    Start-Sleep -Seconds 5

    if (-not $env:OPENDATA_PG_URL) { $env:OPENDATA_PG_URL = 'jdbc:postgresql://localhost:54329/opendata' }
    if (-not $env:OPENDATA_CH_URL) { $env:OPENDATA_CH_URL = 'jdbc:clickhouse://localhost:8123/opendata' }
    if (-not $env:OPENDATA_DREMIO_URL) { $env:OPENDATA_DREMIO_URL = 'jdbc:arrow-flight-sql://localhost:32010/?useEncryption=false' }
}

Invoke-Gradle @("-PossIdePath=$platform", '--console=plain', ':opendata:db:test')
Write-Host 'TESTS OK' -ForegroundColor Green
Write-Host 'Отчёт: opendata\db\build\reports\tests\test\index.html'
