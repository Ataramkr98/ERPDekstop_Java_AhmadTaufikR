@echo off
REM Runs scripts\Start-Erp.ps1 -Mode Desktop with whichever PowerShell is installed.
setlocal
set "PS_BIN="
where pwsh >nul 2>&1 && set "PS_BIN=pwsh"
if not defined PS_BIN where powershell >nul 2>&1 && set "PS_BIN=powershell"
if not defined PS_BIN (
    echo [erp] Neither pwsh nor powershell was found on PATH. Install PowerShell. 1>&2
    exit /b 1
)
"%PS_BIN%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\Start-Erp.ps1" -Mode Desktop %*
exit /b %ERRORLEVEL%