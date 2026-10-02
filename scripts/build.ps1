<#
.SYNOPSIS
  Сборка OpenData IDE для Windows (ТЗ, раздел 29): проверка JDK, загрузка открытой платформы IntelliJ (Apache 2.0),
  сборка и тесты DB-плагина, сборка продукта и ZIP-дистрибутива.
.EXAMPLE
  .\scripts\build.ps1
  .\scripts\build.ps1 -SkipTests
  .\scripts\build.ps1 -PlatformHome D:\idea-oss-2026.2.3
#>
param(
    [switch]$SkipTests,
    [string]$PlatformHome = $env:OPENDATA_PLATFORM_HOME
)
. "$PSScriptRoot\common.ps1"

Write-Step '1/4 JDK'
$null = Assert-Jdk

Write-Step '2/4 Открытая платформа IntelliJ'
$platform = Get-OssPlatform -Os 'windows' -Explicit $PlatformHome
$info = Get-Content (Join-Path $platform 'product-info.json') -Raw | ConvertFrom-Json
Write-Host ("Платформа: {0} {1} ({2}-{3})" -f $info.name, $info.version, $info.productCode, $info.buildNumber)
$since = [int](Get-GradleProperty 'pluginSinceBuild')
if ([int]($info.buildNumber -split '\.')[0] -lt $since) {
    Fail "Несовместимая платформа: build $($info.buildNumber) < pluginSinceBuild $since (gradle.properties)"
}

Write-Step '3/4 DB-плагин opendata-db'
$gradleArgs = @("-PossIdePath=$platform", '--console=plain', ':opendata:db:buildPlugin')
if (-not $SkipTests) { $gradleArgs += ':opendata:db:test' }
Invoke-Gradle $gradleArgs

Write-Step '4/4 Продукт OpenData IDE'
Invoke-Gradle @("-PossIdePath=$platform", '-PproductOs=windows', '--console=plain', 'assembleProduct')

$version = Get-GradleProperty 'version'
Write-Host ''
Write-Host 'BUILD OK' -ForegroundColor Green
Write-Host "IDE:          $(Get-ProductDir 'windows')\bin\opendata64.exe"
Write-Host "Дистрибутив:  $script:ProjectRoot\build\distributions\OpenData-IDE-$version-windows-x64.zip"
