<#
.SYNOPSIS
  Запуск собранной OpenData IDE (build\product\windows\OpenData-IDE).
.PARAMETER Check
  Только проверка запуска: IDE стартует, загружает плагины и пишет отчёт (RESULT=OK) — без подключения к СУБД.
.PARAMETER Smoke
  Самопроверка: id источника данных из настроек IDE. IDE раскроет его в Database Explorer, выполнит запрос
  из консоли (результат в DataGrid), откроет таблицу -Table и запишет отчёт в build\poc\diagnostics.txt.
.EXAMPLE
  .\scripts\run.ps1
  .\scripts\run.ps1 -Check
  .\scripts\run.ps1 -Smoke pg-local -Table public.opendata_test
  .\scripts\run.ps1 -Smoke ch-local -Table opendata.events -Sql 'SELECT number AS id FROM numbers(100)'
#>
param(
    [switch]$Check,
    [string]$Smoke,
    [string]$Table,
    [string]$Sql
)
. "$PSScriptRoot\common.ps1"

$ide = Get-ProductDir 'windows'
$exe = Join-Path $ide 'bin\opendata64.exe'
if (-not (Test-Path $exe)) { Fail "IDE не собрана: запустите .\scripts\build.ps1" }

if (-not $Smoke -and -not $Check) {
    Start-Process -FilePath $exe
    Write-Host "Запущена OpenData IDE: $exe"
    return
}

$report = Join-Path $script:ProjectRoot 'build\poc\diagnostics.txt'
New-Item -ItemType Directory -Force (Split-Path $report) | Out-Null
Remove-Item $report -ErrorAction SilentlyContinue
$vmFile = Join-Path $script:ProjectRoot 'build\poc\smoke.vmoptions'
$lines = @("-Dopendata.diagnostics.file=$report")
if ($Smoke) { $lines += "-Dopendata.smoke=$Smoke" }
if ($Table) { $lines += "-Dopendata.smoke.table=$Table" }
if ($Sql) { $lines += "-Dopendata.smoke.sql=$Sql" }
Set-Content -Path $vmFile -Value $lines -Encoding ASCII
$env:OPENDATA_VM_OPTIONS = $vmFile
$proc = Start-Process -FilePath $exe -PassThru
Write-Host "Самопроверка запущена (PID $($proc.Id)), ожидание отчёта…"
for ($i = 0; $i -lt 120 -and -not (Test-Path $report); $i++) { Start-Sleep -Seconds 2 }
if (-not (Test-Path $report)) { Fail "Отчёт не создан за 4 минуты: см. журнал IDE (Help → Show Log)" }
Start-Sleep -Seconds 2
Get-Content $report | Out-Host
Stop-Process -Id $proc.Id -ErrorAction SilentlyContinue
$marker = if ($Smoke) { 'SMOKE=OK' } else { 'RESULT=OK' }
if ((Get-Content $report -Raw) -match $marker) { Write-Host "$marker" -ForegroundColor Green } else { Fail 'Самопроверка не пройдена' }
