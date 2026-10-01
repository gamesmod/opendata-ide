<#
.SYNOPSIS
  Сборка OpenData IDE (ТЗ, раздел 29): проверка JDK, поиск локальной IDE/DataGrip и
  Database Tools, проверка совместимости build-линейки, research, сборка и тесты.
.EXAMPLE
  .\scripts\build.ps1
  .\scripts\build.ps1 -IdeHome "C:\Program Files\JetBrains\DataGrip 2025.2" -SkipTests
#>
param(
    [string]$IdeHome,
    [string]$CommunityHome = $env:INTELLIJ_COMMUNITY_HOME,
    [switch]$SkipTests,
    [switch]$SkipResearch
)
. "$PSScriptRoot\common.ps1"

Write-Step '1/5 JDK'
$java = Assert-Jdk

Write-Step '2/5 JetBrains IDE / DataGrip'
$ide = Find-JetBrainsIde $IdeHome
Write-Host "IDE_HOME: $ide"

Write-Step '3/5 Database Tools и совместимость'
$null = Invoke-Research -Java $java -IdeHome $ide -CheckOnly
$props = Assert-Compatibility $ide

if ($CommunityHome) {
    if (Test-Path (Join-Path $CommunityHome 'grid')) { Write-Host "intellij-community: $CommunityHome" }
    else { Write-Host "WARN: '$CommunityHome' не похож на intellij-community (нет grid/)" -ForegroundColor Yellow }
} else {
    Write-Host 'intellij-community: не задан (не требуется для сборки плагина; нужен для этапа 5 / анализа исходников grid)'
}

if (-not $SkipResearch) {
    Write-Step '4/5 Research (docs/*)'
    $rc = Invoke-Research -Java $java -IdeHome $ide -CommunityHome $CommunityHome
    if ($rc -ne 0) { Fail "Research завершился с кодом $rc" }
} else { Write-Step '4/5 Research пропущен' }

Write-Step '5/5 Gradle'
$jbr = Get-IdeJbrJava $ide
$gradleArgs = @("-PlocalIdePath=$ide", '--console=plain')
if ($jbr) { $gradleArgs += "-Porg.gradle.java.installations.paths=$(Split-Path (Split-Path $jbr -Parent) -Parent)" }
$gradleArgs += ':opendata:integration:buildPlugin'
if (-not $SkipTests) { $gradleArgs += ':opendata:integration:test' }
Invoke-Gradle $gradleArgs

$zip = Get-ChildItem (Join-Path $script:ProjectRoot 'opendata/integration/build/distributions') -Filter *.zip -ErrorAction SilentlyContinue | Select-Object -First 1
Write-Host ''
Write-Host 'BUILD OK' -ForegroundColor Green
if ($zip) { Write-Host "Плагин: $($zip.FullName)" }
Write-Host 'Обновите статус в docs/compatibility-matrix.md: research OK -> build OK'
