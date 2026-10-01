<#
.SYNOPSIS
  Этап 0 (Research): сканирует локальную JetBrains IDE / DataGrip и генерирует
  docs/jetbrains-db-analysis.md, docs/grid-analysis.md, docs/plugin-dependencies.md,
  docs/compatibility-matrix.md и docs/research/*.
.EXAMPLE
  .\scripts\research.ps1
  .\scripts\research.ps1 -IdeHome "C:\Program Files\JetBrains\DataGrip 2025.2" -CommunityHome D:\src\intellij-community
#>
param(
    [string]$IdeHome,
    [string]$CommunityHome = $env:INTELLIJ_COMMUNITY_HOME
)
. "$PSScriptRoot\common.ps1"

Write-Step 'JDK'
$java = Assert-Jdk

Write-Step 'JetBrains IDE / DataGrip'
$ide = Find-JetBrainsIde $IdeHome
Write-Host "IDE_HOME: $ide"

if ($CommunityHome -and -not (Test-Path (Join-Path $CommunityHome 'grid'))) {
    Write-Host "WARN: в '$CommunityHome' нет каталога grid/ — раздел исходников будет пустым" -ForegroundColor Yellow
}

Write-Step 'Research'
$rc = Invoke-Research -Java $java -IdeHome $ide -CommunityHome $CommunityHome
if ($rc -eq 3) { Fail 'Database Tools and SQL не найден в установке — см. docs/jetbrains-db-analysis.md' }
if ($rc -ne 0) { Fail "Research завершился с кодом $rc" }
Write-Host ''
Write-Host 'Готово. Откройте docs/jetbrains-db-analysis.md (раздел 5 — матрица переиспользования).' -ForegroundColor Green
