param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location $projectRoot
try {
    if (-not $SkipBuild) {
        $savedEnvironment = @{}
        foreach ($name in @('DATABASE_URL','DB_JDBC_URL','DB_PASSWORD','DB_USERNAME','ERP_KEY_DIR')) {
            $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name)
            [Environment]::SetEnvironmentVariable($name, $null)
        }
        try {
            & ./mvnw.cmd verify '-B'
            if ($LASTEXITCODE -ne 0) { throw 'Verification failed.' }
        } finally {
            foreach ($name in $savedEnvironment.Keys) { [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name]) }
        }
    }
    $jdk = $env:JAVA_HOME
    if (-not $jdk) {
        $settings = & java '-XshowSettings:properties' '-version' 2>&1
        $line = $settings | ForEach-Object { "$_" } | Where-Object { $_ -match '^\s*java.home\s*=' } | Select-Object -First 1
        $jdk = ($line -replace '^\s*java.home\s*=\s*','').Trim()
    }
    $jpackage = Join-Path $jdk 'bin/jpackage.exe'
    if (-not (Test-Path -LiteralPath $jpackage)) { throw 'Set JAVA_HOME to a JDK containing jpackage.' }
    $jar = Join-Path $projectRoot 'erp-desktop/target/erp-desktop-1.0.0-SNAPSHOT-runner.jar'
    if (-not (Test-Path -LiteralPath $jar)) { throw 'Build the application first.' }
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $staging = Join-Path $projectRoot ".local/package-$stamp"
    $destination = Join-Path $projectRoot "dist/portfolio-$stamp"
    New-Item -ItemType Directory -Force -Path $staging,$destination | Out-Null
    Copy-Item -LiteralPath $jar -Destination $staging
    $demoLauncher = Join-Path $staging 'demo-launcher.properties'
    @('main-class=com.erp.desktop.Main', 'java-options=-Xmx768m -Dquarkus.profile=demo') | Set-Content -LiteralPath $demoLauncher -Encoding ASCII
    # SmallRye loads configuration through the jar filesystem provider. jdk.zipfs
    # is discovered at runtime and must be included explicitly in the app image.
    & $jpackage --type app-image --name ERP-Workspace --app-version 1.0.0 --vendor 'ERP Workspace' --dest $destination --input $staging --main-jar 'erp-desktop-1.0.0-SNAPSHOT-runner.jar' --main-class com.erp.desktop.Main --add-launcher "ERP-Demo=$demoLauncher" --add-modules 'java.base,java.desktop,java.net.http,java.naming,java.sql,java.management,java.security.jgss,java.instrument,jdk.unsupported,jdk.crypto.ec,jdk.charsets,java.xml,java.logging,java.transaction.xa,jdk.management,java.rmi,jdk.zipfs' --jlink-options '--strip-debug --no-man-pages --no-header-files' --java-options '-Xmx768m'
    if ($LASTEXITCODE -ne 0) { throw 'Native packaging failed.' }
    $bundle = Join-Path $destination 'ERP-Workspace'
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'Configure-Neon.ps1') -Destination $bundle
    Copy-Item -LiteralPath (Join-Path $projectRoot 'NEON-SETUP.md') -Destination $bundle
    Write-Output "Native application: $bundle/ERP-Workspace.exe"
} finally { Pop-Location }
