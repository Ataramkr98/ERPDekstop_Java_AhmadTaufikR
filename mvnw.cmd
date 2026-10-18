@echo off
REM Runs scripts\Maven.ps1 with whichever PowerShell is actually installed.
REM PowerShell 7 (pwsh) is preferred, but Windows PowerShell 5.1 ships with the OS
REM and runs the same script, so a missing pwsh must not break the documented build.
setlocal
set "PS_BIN="
where pwsh >nul 2>&1 && set "PS_BIN=pwsh"
if not defined PS_BIN where powershell >nul 2>&1 && set "PS_BIN=powershell"
if not defined PS_BIN (
    echo [erp] Neither pwsh nor powershell was found on PATH. Install PowerShell. 1>&2
    exit /b 1
)
"%PS_BIN%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\Maven.ps1" %*
exit /b %ERRORLEVEL%