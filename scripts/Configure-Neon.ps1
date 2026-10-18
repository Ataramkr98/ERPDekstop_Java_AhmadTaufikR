param([Security.SecureString]$ConnectionString)
$ErrorActionPreference = 'Stop'
if (-not $ConnectionString) { $ConnectionString = Read-Host 'Paste your Neon PostgreSQL connection URI' -AsSecureString }
$profileRoot = Join-Path $env:LOCALAPPDATA 'ERP-Desktop'
New-Item -ItemType Directory -Force -Path $profileRoot | Out-Null
$credential = [PSCredential]::new('DATABASE_URL', $ConnectionString)
$credential | Export-Clixml -LiteralPath (Join-Path $profileRoot 'neon.credential.xml')
Write-Host 'Neon connection saved with Windows account encryption. Run run-desktop.cmd.'
