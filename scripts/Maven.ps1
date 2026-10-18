$ErrorActionPreference = 'Stop'
$mavenArguments = $args
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) {
    $java = Get-Command java -ErrorAction SilentlyContinue
    if (-not $java) { throw 'Install JDK 21 or later and set JAVA_HOME.' }
    $settings = & $java.Source '-XshowSettings:properties' '-version' 2>&1
    $homeLine = $settings | ForEach-Object { "$_" } | Where-Object { $_ -match '^\s*java.home\s*=\s*(.+)$' } | Select-Object -First 1
    if (-not $homeLine) { throw 'Could not determine JAVA_HOME.' }
    $env:JAVA_HOME = ($homeLine -replace '^\s*java.home\s*=\s*','').Trim()
    if (-not (Test-Path (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) { throw 'JAVA_HOME must point to a JDK, including javac.' }
}
$maven = Join-Path $projectRoot '.tools/maven/bin/mvn.cmd'
if (-not (Test-Path -LiteralPath $maven)) {
    $installed = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $installed) { throw 'Install Apache Maven 3.9+ on PATH, or place it in .tools/maven.' }
    $maven = $installed.Source
}
$proxyOptions = @()
foreach ($scheme in @('http','https')) {
    $rawProxy = [Environment]::GetEnvironmentVariable($scheme.ToUpper() + '_PROXY')
    if ($rawProxy) {
        $proxy = [Uri]$rawProxy
        $proxyOptions += "-D$scheme.proxyHost=$($proxy.Host)"
        $proxyOptions += "-D$scheme.proxyPort=$($proxy.Port)"
    }
}
if ($proxyOptions.Count) {
    $env:MAVEN_OPTS = ($env:MAVEN_OPTS + ' ' + ($proxyOptions -join ' ') + ' "-Dhttp.nonProxyHosts=localhost|127.0.0.1"')
}
Push-Location $projectRoot
try { & $maven @mavenArguments; exit $LASTEXITCODE } finally { Pop-Location }
