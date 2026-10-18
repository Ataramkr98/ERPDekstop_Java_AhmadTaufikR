param(
    [string]$PostgresBin = 'C:\Program Files\PostgreSQL\18\bin',
    [string]$JavaHome = $env:JAVA_HOME,
    [switch]$CaptureSignIn,
    [switch]$Embedded,
    [string]$NativeDemoLauncher
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$qaRoot = Join-Path $projectRoot '.local\portfolio-qa'
$dataDirectory = Join-Path $qaRoot 'postgres'
New-Item -ItemType Directory -Force -Path $qaRoot | Out-Null
if (-not $JavaHome) { $JavaHome = ((& java -XshowSettings:properties -version 2>&1 | ForEach-Object { "$_" } | Where-Object { $_ -match '^\s*java.home\s*=' } | Select-Object -First 1) -replace '^\s*java.home\s*=\s*','').Trim() }
$java = Join-Path $JavaHome 'bin\java.exe'
if (-not (Test-Path -LiteralPath $java)) { throw 'Specify -JavaHome with a JDK 21 path.' }
foreach ($binary in @('initdb.exe','pg_ctl.exe','psql.exe')) { if (-not (Test-Path (Join-Path $PostgresBin $binary))) { throw "PostgreSQL executable not found: $binary" } }
function Get-FreePort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback,0)
    $listener.Start()
    try { return $listener.LocalEndpoint.Port } finally { $listener.Stop() }
}
$dbPort = Get-FreePort
$apiPort = Get-FreePort
$backend = $null
$databaseStarted = $false
Push-Location $projectRoot
try {
    if (-not (Test-Path (Join-Path $dataDirectory 'PG_VERSION'))) {
        & (Join-Path $PostgresBin 'initdb.exe') -D $dataDirectory -U portfolio -A trust -E UTF8 --no-locale --no-sync *> (Join-Path $qaRoot 'init.log')
        if ($LASTEXITCODE) { throw 'Disposable PostgreSQL initialization failed. See .local/portfolio-qa/init.log.' }
    }
    # Wait for pg_ctl itself, not inherited server pipes. Redirected files keep
    # PowerShell from waiting for PostgreSQL's entire lifetime after startup.
    $controlArguments = @('-D', ('"' + $dataDirectory + '"'), '-l', ('"' + (Join-Path $qaRoot 'postgres.log') + '"'), '-o', ('"' + "-p $dbPort -h 127.0.0.1 -c shared_buffers=32MB -c max_connections=20" + '"'), '-w', 'start')
    $control = Start-Process -FilePath (Join-Path $PostgresBin 'pg_ctl.exe') -ArgumentList $controlArguments -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $qaRoot 'control.log') -RedirectStandardError (Join-Path $qaRoot 'control.err')
    if (-not $control.WaitForExit(90000) -or $control.ExitCode) { throw 'Disposable PostgreSQL startup failed.' }
    $databaseStarted = $true
    Write-Output 'QA: disposable PostgreSQL started.'
    $databaseExists = & (Join-Path $PostgresBin 'psql.exe') -h 127.0.0.1 -p $dbPort -U portfolio -d postgres -w -At -c "SELECT count(*) FROM pg_database WHERE datname='erp_portfolio'"
    if ($databaseExists -ne '1') {
        & (Join-Path $PostgresBin 'psql.exe') -h 127.0.0.1 -p $dbPort -U portfolio -d postgres -w -c 'CREATE DATABASE erp_portfolio'
        if ($LASTEXITCODE) { throw 'Disposable database creation failed.' }
    }
    $keys = Join-Path $qaRoot 'keys'; New-Item -ItemType Directory -Force -Path $keys | Out-Null
    if (-not (Test-Path (Join-Path $keys 'private.pem'))) {
        $rsa = [System.Security.Cryptography.RSA]::Create(2048)
        try {
            $privateText = [Convert]::ToBase64String($rsa.ExportPkcs8PrivateKey(),[Base64FormattingOptions]::InsertLineBreaks)
            $publicText = [Convert]::ToBase64String($rsa.ExportSubjectPublicKeyInfo(),[Base64FormattingOptions]::InsertLineBreaks)
            [IO.File]::WriteAllText((Join-Path $keys 'private.pem'), "-----BEGIN PRIVATE KEY-----`n$privateText`n-----END PRIVATE KEY-----`n")
            [IO.File]::WriteAllText((Join-Path $keys 'public.pem'), "-----BEGIN PUBLIC KEY-----`n$publicText`n-----END PUBLIC KEY-----`n")
        } finally { $rsa.Dispose() }
    }
    # This URI belongs only to the disposable loopback cluster, whose authentication is trust.
    $env:DATABASE_URL = "postgresql://portfolio:portfolio@127.0.0.1:$dbPort/erp_portfolio"
    $env:ERP_KEY_DIR = $keys; $env:ERP_PORT = "$apiPort"; $env:ERP_API_URL = "http://127.0.0.1:$apiPort"
    Write-Output 'QA: isolated database and signing keys are ready.'
    if (-not $Embedded) {
        if ($NativeDemoLauncher) {
            if (-not (Test-Path -LiteralPath $NativeDemoLauncher -PathType Leaf)) { throw 'The native demo launcher does not exist.' }
            $backend = Start-Process -FilePath $NativeDemoLauncher -WorkingDirectory $projectRoot -WindowStyle Hidden -PassThru
        } else {
            $backendArguments = @('-Xmx512m', '-XX:ActiveProcessorCount=4', '-Dquarkus.profile=demo', "-Dquarkus.http.port=$apiPort", '-jar', 'erp-backend/target/quarkus-app/quarkus-run.jar')
            $backend = Start-Process -FilePath $java -ArgumentList $backendArguments -WorkingDirectory $projectRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $qaRoot 'backend.log') -RedirectStandardError (Join-Path $qaRoot 'backend.err')
        }
        $ready = $false; $deadline = [DateTime]::UtcNow.AddMinutes(3)
        while ([DateTime]::UtcNow -lt $deadline) {
            if ($backend.HasExited) { throw 'QA service exited. See .local/portfolio-qa backend or native logs.' }
            try { $response = Invoke-RestMethod -Uri "$env:ERP_API_URL/api/demo/status" -TimeoutSec 3; if ($response.enabled) { $ready = $true; break } } catch { }
            Start-Sleep -Seconds 1
        }
        if (-not $ready) { throw 'QA service did not become ready within three minutes.' }
    }
    $classpath = (Get-Content -LiteralPath 'erp-desktop/target/cp.txt' -Raw).Trim()
    $fullClasspath = (@('erp-desktop/target/classes','erp-desktop/target/test-classes','erp-core/target/classes') + $classpath) -join ';'
    if ($Embedded) { $fullClasspath = @('erp-desktop/target/erp-desktop-1.0.0-SNAPSHOT-runner.jar','erp-desktop/target/test-classes') -join ';' }
    $mainClass = if ($CaptureSignIn -or $Embedded -or $NativeDemoLauncher) { $env:ERP_CAPTURE = 'demo'; 'com.erp.desktop.PortfolioCapture' } else { 'com.erp.desktop.PortfolioScenario' }
    $argFile = Join-Path $qaRoot 'capture.args'
    @('-Xmx768m', '-XX:ActiveProcessorCount=4', '-Djava.awt.headless=false', '-Dquarkus.profile=demo', '-cp', ('"' + $fullClasspath.Replace('\','\\') + '"'), $mainClass) | Set-Content -LiteralPath $argFile -Encoding UTF8
    & $java "@$argFile"
    if ($LASTEXITCODE) { throw 'The UI acceptance scenario failed.' }
} finally {
    if ($backend -and $NativeDemoLauncher) {
        # jpackage uses a child launcher on Windows. Close only descendants of
        # this QA process whose executable matches the explicitly supplied path.
        $nativePath = (Resolve-Path -LiteralPath $NativeDemoLauncher).Path
        $queue = [Collections.Generic.Queue[int]]::new(); $queue.Enqueue($backend.Id)
        $children = [Collections.Generic.List[int]]::new()
        while ($queue.Count) {
            $parentPid = $queue.Dequeue()
            foreach ($child in @(Get-CimInstance Win32_Process -Filter "ParentProcessId=$parentPid" -ErrorAction SilentlyContinue)) {
                if ($child.ExecutablePath -eq $nativePath) { $children.Add([int]$child.ProcessId); $queue.Enqueue([int]$child.ProcessId) }
            }
        }
        for ($index = $children.Count - 1; $index -ge 0; $index--) { Stop-Process -Id $children[$index] -ErrorAction SilentlyContinue }
    }
    if ($backend -and -not $backend.HasExited) { Stop-Process -Id $backend.Id -ErrorAction SilentlyContinue }
    if ($databaseStarted) {
        $stopArguments = @('-D', ('"' + $dataDirectory + '"'), '-m', 'fast', '-w', 'stop')
        $stopControl = Start-Process -FilePath (Join-Path $PostgresBin 'pg_ctl.exe') -ArgumentList $stopArguments -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $qaRoot 'stop.log') -RedirectStandardError (Join-Path $qaRoot 'stop.err')
        $stopControl.WaitForExit(30000) | Out-Null
    }
    Pop-Location
}
