# Общие функции скриптов OpenData IDE (Windows PowerShell 5.1+ / PowerShell 7+).
# Подключается через: . "$PSScriptRoot\common.ps1"

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$script:ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$script:RequiredJdk = 21
# $IsWindows/$IsMacOS/$IsLinux отсутствуют в Windows PowerShell 5.1 (и запрещены StrictMode) — вычисляем сами.
$script:OnWindows = ($env:OS -eq 'Windows_NT')
$script:OnMac = (-not $script:OnWindows) -and (Test-Path '/System/Library/CoreServices')
$script:OnLinux = (-not $script:OnWindows) -and (-not $script:OnMac)

function Write-Step([string]$Text) {
    Write-Host ""
    Write-Host "==> $Text" -ForegroundColor Cyan
}

function Fail([string]$Text) {
    Write-Host ""
    Write-Host "ERROR: $Text" -ForegroundColor Red
    exit 1
}

function Get-GradleProperty([string]$Name) {
    $file = Join-Path $script:ProjectRoot 'gradle.properties'
    foreach ($line in Get-Content $file) {
        if ($line -match "^\s*$([regex]::Escape($Name))\s*=\s*(.*)$") { return $Matches[1].Trim() }
    }
    return $null
}

function Read-Properties([string]$Path) {
    $map = @{}
    if (-not (Test-Path $Path)) { return $map }
    foreach ($line in Get-Content $Path) {
        if ($line -match '^\s*#' -or $line -notmatch '=') { continue }
        $i = $line.IndexOf('=')
        # java.util.Properties экранирует ':' и '\' — снимаем экранирование
        $map[$line.Substring(0, $i).Trim()] = $line.Substring($i + 1).Trim() -replace '\\(.)', '$1'
    }
    return $map
}

# Возвращает major-версию Java по пути к java(.exe) или $null.
function Get-JavaMajor([string]$JavaExe) {
    if (-not $JavaExe -or -not (Test-Path $JavaExe)) { return $null }
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $out = & $JavaExe -XshowSettings:properties -version 2>&1 | Out-String
    } finally { $ErrorActionPreference = $prev }
    if ($out -match 'java\.specification\.version\s*=\s*(\d+)') { return [int]$Matches[1] }
    return $null
}

function Test-HasJavac([string]$JavaExe) {
    $dir = Split-Path $JavaExe -Parent
    return (Test-Path (Join-Path $dir 'javac.exe')) -or (Test-Path (Join-Path $dir 'javac'))
}

# Находит JDK >= 21: JAVA_HOME, затем java в PATH. Возвращает путь к java(.exe).
function Find-Jdk {
    $candidates = @()
    if ($env:JAVA_HOME) {
        $candidates += (Join-Path $env:JAVA_HOME 'bin\java.exe')
        $candidates += (Join-Path $env:JAVA_HOME 'bin/java')
    }
    $onPath = Get-Command java -ErrorAction SilentlyContinue
    if ($onPath) { $candidates += $onPath.Source }
    foreach ($c in $candidates) {
        $major = Get-JavaMajor $c
        if ($major -and $major -ge $script:RequiredJdk -and (Test-HasJavac $c)) { return $c }
    }
    return $null
}

function Assert-Jdk {
    $java = Find-Jdk
    if (-not $java) {
        Fail ("Не найден JDK $($script:RequiredJdk)+ (нужен именно JDK с javac, не JRE). " +
              "Установите, например, Eclipse Temurin 21 и задайте JAVA_HOME.")
    }
    $major = Get-JavaMajor $java
    Write-Host "JDK: $java (Java $major)"
    return $java
}

# Каталог IDE считается подходящим, если содержит lib\ и product-info.json (Windows/Linux)
# или Contents\Resources\product-info.json (macOS .app).
function Test-IdeHome([string]$Path) {
    if (-not $Path -or -not (Test-Path $Path)) { return $false }
    return (Test-Path (Join-Path $Path 'product-info.json')) -or
           (Test-Path (Join-Path $Path 'Contents/Resources/product-info.json'))
}

function Get-IdeInfo([string]$Path) {
    $pi = Join-Path $Path 'product-info.json'
    if (-not (Test-Path $pi)) { $pi = Join-Path $Path 'Contents/Resources/product-info.json' }
    $json = Get-Content $pi -Raw | ConvertFrom-Json
    $hasDb = (Test-Path (Join-Path $Path 'plugins/DatabaseTools')) -or (Test-Path (Join-Path $Path 'Contents/plugins/DatabaseTools'))
    return [pscustomobject]@{
        Path = $Path; Name = $json.name; Version = $json.version
        Build = $json.buildNumber; Code = $json.productCode; HasDatabaseDir = $hasDb
    }
}

# Порядок поиска (ТЗ 8.2): параметр → JETBRAINS_IDE_HOME → DATAGRIP_HOME → стандартные каталоги установки.
# Пути конкретного пользователя не хардкодятся: перебираются только стандартные корни установщиков.
function Find-JetBrainsIde([string]$Explicit) {
    foreach ($p in @($Explicit, $env:JETBRAINS_IDE_HOME, $env:DATAGRIP_HOME)) {
        if ($p) {
            if (Test-IdeHome $p) { return (Resolve-Path $p).Path }
            Fail "Каталог IDE '$p' не содержит product-info.json"
        }
    }
    $roots = @()
    if ($env:LOCALAPPDATA) {
        $roots += (Join-Path $env:LOCALAPPDATA 'Programs')                         # Toolbox 2.x / user install
        $roots += (Join-Path $env:LOCALAPPDATA 'JetBrains\Toolbox\apps')          # Toolbox 1.x
    }
    if ($env:ProgramFiles) { $roots += (Join-Path $env:ProgramFiles 'JetBrains') }
    if ($script:OnMac) { $roots += '/Applications'; $roots += (Join-Path $HOME 'Applications') }
    if ($script:OnLinux) { $roots += '/opt'; $roots += (Join-Path $HOME '.local/share/JetBrains/Toolbox/apps') }

    $found = @()
    foreach ($r in $roots) {
        if (-not (Test-Path $r)) { continue }
        foreach ($d in Get-ChildItem $r -Directory -Recurse -Depth 3 -ErrorAction SilentlyContinue) {
            if ($d.FullName -notmatch '(?i)datagrip|idea|intellij') { continue }
            if (Test-IdeHome $d.FullName) { $found += Get-IdeInfo $d.FullName }
        }
    }
    $found = @($found | Where-Object { $_.HasDatabaseDir -and ($_.Code -eq 'DB' -or $_.Code -eq 'IU' -or $_.Code -eq 'IC' -or $_.Code -eq 'IE') })
    if ($found.Count -eq 0) {
        Fail ("Не найдена установка IntelliJ IDEA / DataGrip с Database Tools. " +
              "Укажите -IdeHome <путь> или переменную JETBRAINS_IDE_HOME / DATAGRIP_HOME.")
    }
    Write-Host "Найдены установки:"
    $found | Sort-Object Build -Descending | ForEach-Object { Write-Host ("  {0} {1} ({2}-{3})  {4}" -f $_.Name, $_.Version, $_.Code, $_.Build, $_.Path) }
    # Предпочитаем DataGrip, затем самую свежую сборку.
    $best = $found | Sort-Object @{Expression = { if ($_.Code -eq 'DB') { 0 } else { 1 } }}, @{Expression = 'Build'; Descending = $true} | Select-Object -First 1
    return $best.Path
}

function Get-IdeJbrJava([string]$IdeHome) {
    foreach ($p in @('jbr\bin\java.exe', 'jbr/bin/java', 'Contents/jbr/Contents/Home/bin/java')) {
        $f = Join-Path $IdeHome $p
        if (Test-Path $f) { return $f }
    }
    return $null
}

function Invoke-Research([string]$Java, [string]$IdeHome, [string]$CommunityHome, [switch]$CheckOnly) {
    $argsList = @("$script:ProjectRoot/tools/research/Research.java", '--ide', $IdeHome,
                  '--out', "$script:ProjectRoot/docs", '--state', "$script:ProjectRoot/build/research",
                  '--gradle', (Get-WrapperGradleVersion))
    if ($CommunityHome) { $argsList += @('--community', $CommunityHome) }
    if ($CheckOnly) {
        $argsList += '--check'
        # не допускаем проверку совместимости по устаревшему результату прошлого запуска
        Remove-Item (Join-Path $script:ProjectRoot 'build/research/ide.properties') -ErrorAction SilentlyContinue
    }
    & $Java @argsList | Out-Host
    return $LASTEXITCODE
}

function Get-WrapperGradleVersion {
    $p = Join-Path $script:ProjectRoot 'gradle/wrapper/gradle-wrapper.properties'
    if ((Get-Content $p -Raw) -match 'gradle-([\d.]+)-(bin|all)\.zip') { return $Matches[1] }
    return 'unknown'
}

function Invoke-Gradle([string[]]$GradleArgs) {
    $gradlew = if ($script:OnWindows) { Join-Path $script:ProjectRoot 'gradlew.bat' } else { Join-Path $script:ProjectRoot 'gradlew' }
    Write-Host "> gradlew $($GradleArgs -join ' ')"
    Push-Location $script:ProjectRoot
    try { & $gradlew @GradleArgs | Out-Host } finally { Pop-Location }
    if ($LASTEXITCODE -ne 0) { Fail "Gradle завершился с кодом $LASTEXITCODE" }
}

# Проверка совместимости build-линейки (ТЗ 29/30).
function Assert-Compatibility([string]$IdeHome) {
    $props = Read-Properties (Join-Path $script:ProjectRoot 'build/research/ide.properties')
    if ($props.Count -eq 0) { Fail 'Нет build/research/ide.properties — research --check не отработал' }
    Write-Host ("IDE: {0} {1}, build {2}-{3}, JBR {4}" -f $props['ide.name'], $props['ide.version'], $props['ide.productCode'], $props['ide.buildNumber'], $props['jbr.version'])
    if ($props['database.plugin.found'] -ne 'true') {
        Fail ("В установке нет плагина Database Tools and SQL (com.intellij.database). " +
              "Используйте DataGrip или IntelliJ IDEA с этим плагином.")
    }
    Write-Host ("Database Tools: {0} {1} ({2})" -f $props['database.plugin.id'], $props['database.plugin.version'], $props['database.plugin.jar'])
    $since = [int](Get-GradleProperty 'pluginSinceBuild')
    $baseline = 0
    if (-not [int]::TryParse($props['ide.baseline'], [ref]$baseline)) { Fail "Не удалось разобрать build number IDE: $($props['ide.buildNumber'])" }
    if ($baseline -lt $since) {
        Fail ("Несовместимая сборка: IDE baseline $baseline < pluginSinceBuild $since (gradle.properties). " +
              "Обновите IDE или понизьте pluginSinceBuild после проверки docs/compatibility-matrix.md.")
    }
    $jbrMajor = ($props['jbr.version'] -split '\.')[0]
    $jbrInt = 0
    if ([int]::TryParse($jbrMajor, [ref]$jbrInt) -and $jbrInt -lt $script:RequiredJdk) {
        Write-Host "WARN: JBR IDE = Java $jbrMajor < $($script:RequiredJdk): плагин, собранный под Java $($script:RequiredJdk), в этой IDE не загрузится." -ForegroundColor Yellow
    }
    return $props
}
