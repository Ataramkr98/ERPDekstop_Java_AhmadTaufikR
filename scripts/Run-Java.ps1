param(
    [string]$ClasspathFile = 'erp-desktop\target\cp.txt',
    [string]$MainClass = 'com.erp.desktop.PortfolioCapture'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location $projectRoot
try {
    $profileRoot = Join-Path $env:LOCALAPPDATA 'ERP-Desktop'
    $credentialFile = Join-Path $profileRoot 'neon.credential.xml'
    if (-not $env:DATABASE_URL -and (Test-Path -LiteralPath $credentialFile)) {
        $saved = Import-Clixml -LiteralPath $credentialFile
        $env:DATABASE_URL = $saved.GetNetworkCredential().Password
    }
    if (-not $env:ERP_KEY_DIR) { $env:ERP_KEY_DIR = Join-Path $profileRoot 'keys' }
    New-Item -ItemType Directory -Force -Path $env:ERP_KEY_DIR | Out-Null

    $classpath = (Get-Content -LiteralPath $ClasspathFile -Raw).Trim()
    $entries = @('erp-desktop\target\classes', 'erp-desktop\target\test-classes',
                 'erp-core\target\classes', 'erp-core\target\test-classes') |
        Where-Object { Test-Path $_ }
    $fullClasspath = (($entries + $classpath) -join ';')

    $java = $null
    if ($env:JAVA_HOME) { $java = Join-Path $env:JAVA_HOME 'bin\java.exe' }
    if (-not $java -or -not (Test-Path $java)) { $java = (Get-Command java -ErrorAction SilentlyContinue).Source }
    if (-not $java) { throw 'Set JAVA_HOME to a JDK 21 or later installation.' }

    # The classpath is far longer than the Windows 32k command-line limit, so the
    # launcher arguments go through a java argument file instead.
    $argFile = Join-Path $env:TEMP ('erp-java-args-' + [Guid]::NewGuid().ToString('N') + '.txt')
    @(
        '-Djava.awt.headless=false'
        '-cp'
        ('"' + $fullClasspath.Replace('\', '\\') + '"')
        $MainClass
    ) | Set-Content -LiteralPath $argFile -Encoding ASCII
    try { & $java "@$argFile" } finally { Remove-Item -LiteralPath $argFile -ErrorAction SilentlyContinue }
    exit $LASTEXITCODE
} finally { Pop-Location }
