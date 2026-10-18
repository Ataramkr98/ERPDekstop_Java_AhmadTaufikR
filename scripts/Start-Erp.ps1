<#
.SYNOPSIS
    Starts the ERP desktop app, the headless backend, or the portfolio demo.

.DESCRIPTION
    Mode Demo runs the real application against a demo database and turns on the demo
    workspace: the sign-in screen gains a "Coba Demo" button, the demo session gets its
    own tenant (company code DEMO), and "Reset Data Demo" restores the sample dataset.

    The demo database is chosen in this order:
      1. ERP_DEMO_DATABASE_URL / ERP_DEMO_JDBC_URL, if set (a dedicated database);
      2. DATABASE_URL / DB_JDBC_URL, if set (point this at a demo database);
      3. the saved Neon credential.

    Demo mode is a Quarkus profile, and DemoSettings refuses to enable it on a prod or
    live profile, so a production deployment can never expose the credential-free
    demo sign-in.
#>
param(
    [ValidateSet('Desktop', 'Backend', 'Demo')][string]$Mode = 'Desktop',
    [switch]$Build
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent

if ($Build) {
    $savedEnvironment = @{}
    foreach ($name in @('DATABASE_URL','DB_JDBC_URL','DB_PASSWORD','DB_USERNAME','ERP_KEY_DIR')) {
        $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name)
        [Environment]::SetEnvironmentVariable($name, $null)
    }
    try {
        & (Join-Path $projectRoot 'mvnw.cmd') package '-Dquarkus.analytics.disabled=true' '-B'
        if ($LASTEXITCODE -ne 0) { throw 'Build failed.' }
    } finally {
        foreach ($name in $savedEnvironment.Keys) { [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name]) }
    }
}

$profileRoot = Join-Path $env:LOCALAPPDATA 'ERP-Desktop'
$credentialFile = Join-Path $profileRoot 'neon.credential.xml'

if ($Mode -eq 'Demo') {
    # Point the demo at its own database when one is configured, so demo traffic can
    # never share a schema with a real company.
    if ($env:ERP_DEMO_DATABASE_URL) { $env:DATABASE_URL = $env:ERP_DEMO_DATABASE_URL }
    elseif ($env:ERP_DEMO_JDBC_URL) { $env:DB_JDBC_URL = $env:ERP_DEMO_JDBC_URL }
}

if (-not $env:DATABASE_URL -and -not $env:DB_JDBC_URL) {
    if (-not (Test-Path -LiteralPath $credentialFile)) {
        throw 'No database configured. Run scripts/Configure-Neon.ps1, or set DATABASE_URL (demo mode also accepts ERP_DEMO_DATABASE_URL).'
    }
    $saved = Import-Clixml -LiteralPath $credentialFile
    $env:DATABASE_URL = $saved.GetNetworkCredential().Password
}

$env:DB_BUNDLED_POSTGRES = 'false'
# Demo sessions sign their own tokens; give the demo a key directory of its own so it
# cannot reuse a production signing key.
$keyDirName = if ($Mode -eq 'Demo') { 'demo-keys' } else { 'keys' }
$env:ERP_KEY_DIR = Join-Path $profileRoot $keyDirName
New-Item -ItemType Directory -Force -Path $env:ERP_KEY_DIR | Out-Null

$privateKey = Join-Path $env:ERP_KEY_DIR 'private.pem'
$publicKey = Join-Path $env:ERP_KEY_DIR 'public.pem'
if (-not (Test-Path -LiteralPath $privateKey)) {
    $rsa = [System.Security.Cryptography.RSA]::Create(3072)
    try {
        [IO.File]::WriteAllText($privateKey, $rsa.ExportPkcs8PrivateKeyPem())
        [IO.File]::WriteAllText($publicKey, $rsa.ExportSubjectPublicKeyInfoPem())
    } finally { $rsa.Dispose() }
}
if (-not (Test-Path -LiteralPath $publicKey)) { throw 'Signing public key is missing.' }

$javaCommand = Get-Command java -ErrorAction SilentlyContinue
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin/java.exe'))) {
    $javaExecutable = Join-Path $env:JAVA_HOME 'bin/java.exe'
} elseif ($javaCommand) { $javaExecutable = $javaCommand.Source }
else { throw 'Set JAVA_HOME to a JDK 21 or later installation.' }

Push-Location $projectRoot
try {
    if ($Mode -eq 'Backend') {
        $jar = Join-Path $projectRoot 'erp-backend/target/quarkus-app/quarkus-run.jar'
        if (-not (Test-Path -LiteralPath $jar)) { throw 'Run with -Build to create the backend.' }
        & $javaExecutable -jar $jar
    } else {
        $jar = Join-Path $projectRoot 'erp-desktop/target/erp-desktop-1.0.0-SNAPSHOT-runner.jar'
        if (-not (Test-Path -LiteralPath $jar)) { throw 'Run with -Build to create the desktop.' }
        if ($Mode -eq 'Demo') {
            Write-Host '[erp] Demo mode: press "Coba Demo" on the sign-in screen - no password needed.' -ForegroundColor Cyan
            Write-Host '[erp] Demo data lives in its own tenant; use "Reset Data Demo" in the top bar.' -ForegroundColor Cyan
            & $javaExecutable '-Dquarkus.profile=demo' -cp $jar com.erp.desktop.Main
        } else {
            & $javaExecutable -cp $jar com.erp.desktop.Main
        }
    }
    exit $LASTEXITCODE
} finally { Pop-Location }