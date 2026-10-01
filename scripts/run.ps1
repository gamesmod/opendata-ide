<#
.SYNOPSIS
  Запуск IDE-песочницы с Database Tools и плагином OpenData (POC, ТЗ раздел 31).
.PARAMETER Poc
  Открыть служебный проект build/poc/project и после закрытия IDE вывести отчёт
  диагностики (Database Tools loaded / Database Tool Window visible / PostgreSQL dialect).
.EXAMPLE
  .\scripts\run.ps1 -Poc
#>
param(
    [string]$IdeHome,
    [switch]$Poc
)
. "$PSScriptRoot\common.ps1"

$java = Assert-Jdk
$ide = Find-JetBrainsIde $IdeHome
$null = Invoke-Research -Java $java -IdeHome $ide -CheckOnly
$null = Assert-Compatibility $ide

$gradleArgs = @("-PlocalIdePath=$ide", '--console=plain')
$jbr = Get-IdeJbrJava $ide
if ($jbr) { $gradleArgs += "-Porg.gradle.java.installations.paths=$(Split-Path (Split-Path $jbr -Parent) -Parent)" }

$report = Join-Path $script:ProjectRoot 'build/poc/diagnostics.txt'
if ($Poc) {
    $project = Join-Path $script:ProjectRoot 'build/poc/project'
    New-Item -ItemType Directory -Force $project | Out-Null
    Copy-Item (Join-Path $script:ProjectRoot 'docs/poc/*.sql') $project -Force -ErrorAction SilentlyContinue
    Remove-Item $report -ErrorAction SilentlyContinue
    $gradleArgs += "-PopenProject=$project"
    Write-Host 'POC: IDE откроет build/poc/project. Пройдите чек-лист docs/acceptance.md (этап 1), затем закройте IDE.' -ForegroundColor Cyan
}
$gradleArgs += ':opendata:integration:runIde'
Invoke-Gradle $gradleArgs

if ($Poc) {
    Write-Step 'POC diagnostics'
    if (Test-Path $report) {
        $text = Get-Content $report -Raw
        Write-Host $text
        if ($text -match 'RESULT: OK') { Write-Host 'POC (автоматическая часть): OK' -ForegroundColor Green }
        else { Fail 'POC: диагностика не пройдена — см. отчёт выше' }
    } else {
        Fail "Отчёт $report не создан: проект не был открыт или плагин OpenData не загрузился (см. idea.log в build/idea-sandbox)"
    }
}
