@echo off
REM ---------------------------------------------------------------------------
REM Build a Windows EXE installer for the ERP Desktop app.
REM
REM Output:
REM   dist\ERP-Desktop-1.0.0.exe
REM
REM Pipeline:
REM   1. mvnw.cmd package        -> erp-desktop/target/quarkus-app/quarkus-run.jar
REM      (uber-jar with the JavaFX client + Quarkus backend classes, see
REM      erp-desktop/pom.xml -> quarkus.package.jar.type=uber-jar)
REM   2. mvn dependency:copy     -> fetches org.openjfx:javafx-jmods:21.0.8:win
REM      (jlink needs *.jmod files, not jars; openjfx is not in the JDK)
REM   3. jlink                   -> builds a slim runtime image with JDK modules
REM      + javafx.{base,graphics,controls,fxml,media}
REM   4a. jpackage --type app-image -> dist\ERP-Desktop\  (folder layout)
REM   4b. ISCC.exe (Inno Setup)     -> dist\ERP-Desktop-1.0.0-setup.exe
REM      jpackage --type exe would need the WiX Toolset, which is not installed.
REM ---------------------------------------------------------------------------
setlocal EnableExtensions

set "ROOT=%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
set "MAVEN_HOME=%ROOT%.tools\maven"
set "INNO_HOME=C:\Program Files (x86)\Inno Setup 6"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%INNO_HOME%;%PATH%"

set "JFX_VERSION=21.0.8"
set "APP_NAME=ERP-Desktop"
set "APP_VERSION=1.0.0"
set "VENDOR=ERP"

set "DIST=%ROOT%dist"
set "RUNTIME=%ROOT%.tools\runtime"
set "APP_DIR=%ROOT%erp-desktop\target"
REM Uber-jar layout (per erp-desktop/pom.xml -> quarkus.package.jar.type=uber-jar):
REM   target/erp-desktop-1.0.0-SNAPSHOT-runner.jar    <-- self-contained jar (Quarkus
REM   appends "-runner" to the artifactId during quarkus:build)
REM Fast-jar layout (alternative) would put quarkus-run.jar in target\quarkus-app\.
set "APP_JAR=%APP_DIR%\erp-desktop-1.0.0-SNAPSHOT-runner.jar"

if exist "%DIST%"    rmdir /s /q "%DIST%"
if exist "%RUNTIME%" rmdir /s /q "%RUNTIME%"

REM =============================================================================
REM 1. Build the uber-jar
REM =============================================================================
echo.
echo === [1/4] Building uber-jar ===
REM Skip if the uber-jar is already present and newer than the pom files
REM (Quarkus augmentation alone takes ~80s and is not needed for an incremental
REM re-run of jpackage/jlink).
if exist "%APP_JAR%" (
    echo [installer] uber-jar already present, skipping mvn package.
    echo [installer]   %APP_JAR%
) else (
    call "%ROOT%mvnw.cmd" -pl erp-desktop -am -DskipTests package
    if not "%ERRORLEVEL%"=="0" goto :fail
    if not exist "%APP_JAR%" (
        echo [installer] ERROR: %APP_JAR% was not produced.
        echo [installer] Looking for any jar in %APP_DIR%:
        dir /b "%APP_DIR%\*.jar" 2>nul
        goto :fail
    )
    echo [installer] uber-jar OK: %APP_JAR%
)

REM =============================================================================
REM 2. Locate OpenJFX platform jars
REM -----------------------------------------------------------------------------
REM The org.openjfx:javafx-jmods artifact is NOT published on Maven Central;
REM OpenJFX ships the jmods only in their SDK download. But the OpenJFX JARs
REM are normal modules: each carries an Automatic-Module-Name in its manifest
REM (javafx.base, javafx.controls, javafx.graphics, javafx.media), so jlink can
REM treat them as automatic modules directly from the local Maven repo - no
REM separate jmods download required.
REM =============================================================================
echo.
echo === [2/4] Locating OpenJFX %JFX_VERSION% jars ===
set "M2=%USERPROFILE%\.m2\repository"
set "JFX_BASE=%M2%\org\openjfx"
for %%m in (base controls graphics fxml media) do (
    if not exist "%JFX_BASE%\javafx-%%m\%JFX_VERSION%\javafx-%%m-%JFX_VERSION%-win.jar" (
        echo [installer] ERROR: %JFX_BASE%\javafx-%%m\%JFX_VERSION%\javafx-%%m-%JFX_VERSION%-win.jar not found.
        echo [installer] Run mvnw.cmd -pl erp-desktop -am dependency:resolve once to populate the local repo.
        goto :fail
    )
)
echo [installer] OK: OpenJFX platform jars located in %JFX_BASE%

REM =============================================================================
REM 3. Build the custom runtime image with jlink
REM =============================================================================
echo.
echo === [3/4] Building runtime image ===
set "JFX_MODULES=%JFX_BASE%\javafx-base\%JFX_VERSION%\javafx-base-%JFX_VERSION%-win.jar;%JFX_BASE%\javafx-controls\%JFX_VERSION%\javafx-controls-%JFX_VERSION%-win.jar;%JFX_BASE%\javafx-graphics\%JFX_VERSION%\javafx-graphics-%JFX_VERSION%-win.jar;%JFX_BASE%\javafx-fxml\%JFX_VERSION%\javafx-fxml-%JFX_VERSION%-win.jar;%JFX_BASE%\javafx-media\%JFX_VERSION%\javafx-media-%JFX_VERSION%-win.jar"

REM ** The --add-modules list below is the single most important line in this
REM ** script. jlink builds a runtime containing ONLY these modules, so anything
REM ** missing here becomes a NoClassDefFoundError at runtime - and because
REM ** DesktopApp.start() is where it blows up, the symptom is "the window never
REM ** appears" rather than a readable error.
REM **
REM ** This exact list came from:
REM **   jdeps --print-module-deps --multi-release 21 --ignore-missing-deps ^
REM **         erp-desktop\target\erp-desktop-1.0.0-SNAPSHOT-runner.jar
REM ** --> java.base,java.instrument,java.naming,java.net.http,java.rmi,
REM **     java.scripting,java.security.jgss,java.security.sasl,java.sql,
REM **     java.xml.crypto,jdk.compiler,jdk.jconsole,jdk.jfr,jdk.management,
REM **     jdk.net,jdk.unsupported
REM **
REM ** Re-run that command after adding dependencies to erp-desktop, otherwise
REM ** you WILL ship a broken installer. java.net.http is the one that bit us:
REM ** com.erp.desktop.api.ApiClient uses java.net.http.HttpClient, and its
REM ** absence throws in Application.start() -> blank screen, no window.
set "JDK_MODULES=java.base,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,java.xml.crypto,jdk.compiler,jdk.jconsole,jdk.jfr,jdk.management,jdk.net,jdk.unsupported"
set "JFX_MODULE_LIST=javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.media"

"%JAVA_HOME%\bin\jlink.exe" ^
    --module-path "%JAVA_HOME%\jmods;%JFX_MODULES%" ^
    --add-modules %JDK_MODULES%,%JFX_MODULE_LIST% ^
    --strip-debug --no-man-pages --compress=zip-9 ^
    --output "%RUNTIME%"
if not "%ERRORLEVEL%"=="0" goto :fail

REM Guard: fail the build if java.net.http did not make it in. A silent omission
REM here produces an app that starts, crashes in Application.start(), and shows
REM nothing at all - hours of debugging otherwise.
"%RUNTIME%\bin\java.exe" --list-modules | findstr /B "java.net.http@" >nul
if not "%ERRORLEVEL%"=="0" (
    echo [installer] ERROR: bundled runtime is missing java.net.http.
    echo [installer] ApiClient needs it; the app would start and show no window.
    goto :fail
)
echo [installer] runtime image OK: %RUNTIME%

REM =============================================================================
REM 4. Build the app-image with jpackage (no installer yet, just the bundle)
REM -----------------------------------------------------------------------------
REM Why app-image and not --type exe directly?
REM   On Windows, jpackage --type exe requires the WiX Toolset (light.exe,
REM   candle.exe) to build the bundled installer. WiX is NOT installed here,
REM   but Inno Setup 6 IS. So:
REM     4a. jpackage --type app-image  -> dist\ERP-Desktop\    (folder layout)
REM     4b. ISCC.exe (Inno Setup)      -> dist\ERP-Desktop-1.0.0-setup.exe
REM =============================================================================
echo.
echo === [4a/4] Building app-image ===
REM --input must be a directory holding ONLY the jar. Pointing it at
REM erp-desktop\target makes jpackage copy target\_installtest (a full previous
REM install, complete with its own runtime\ and PostgreSQL\) and target\installer
REM into itself -> tens of thousands of files and an apparent hang.
set "APP_INPUT=%ROOT%.tools\appinput"
if exist "%APP_INPUT%" rmdir /s /q "%APP_INPUT%"
mkdir "%APP_INPUT%"
copy /Y "%APP_JAR%" "%APP_INPUT%\" >nul
if not "%ERRORLEVEL%"=="0" (
    echo [installer] ERROR: could not stage %APP_JAR%
    goto :fail
)
"%JAVA_HOME%\bin\jpackage.exe" ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%VENDOR%" ^
    --dest "%DIST%" ^
    --input "%APP_INPUT%" ^
    --main-jar erp-desktop-1.0.0-SNAPSHOT-runner.jar ^
    --main-class com.erp.desktop.Bootstrap ^
    --runtime-image "%RUNTIME%" ^
    --java-options "-Xmx1g" ^
    --java-options "--add-opens=java.base/java.lang=ALL-UNNAMED" ^
    --java-options "--add-opens=java.base/java.nio=ALL-UNNAMED" ^
    --java-options "--add-opens=java.base/java.util=ALL-UNNAMED" ^
    --java-options "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED" ^
    --java-options "--add-opens=java.desktop/java.awt=ALL-UNNAMED"
if not "%ERRORLEVEL%"=="0" goto :fail
if not exist "%DIST%\%APP_NAME%\ERP-Desktop.exe" (
    echo [installer] ERROR: %DIST%\%APP_NAME%\ERP-Desktop.exe was not produced.
    goto :fail
)
echo [installer] app-image OK: %DIST%\%APP_NAME%\

REM =============================================================================
REM 4a.5. Bundle PostgreSQL 18 into the app-image
REM -----------------------------------------------------------------------------
REM com.erp.desktop.Bootstrap looks for a bundled PostgreSQL at
REM   <install-root>/PostgreSQL/18
REM where <install-root> is the parent of java.home (i.e. the dir above
REM "runtime"). Without this, the app falls back to "external server" mode and
REM a clean install on a fresh machine will not have a database.
REM =============================================================================
echo.
echo === [4a.5/4] Bundling PostgreSQL 18 into the app-image ===
set "PG_SRC=C:\Program Files\PostgreSQL\18"
set "PG_DST=%DIST%\%APP_NAME%\PostgreSQL\18"
if not exist "%PG_SRC%\bin\pg_ctl.exe" (
    echo [installer] ERROR: PostgreSQL 18 not found at "%PG_SRC%".
    echo [installer] Edit PG_SRC at the top of this script.
    goto :fail
)
mkdir "%PG_DST%\..\..\PostgreSQL" 2>nul
REM Only the embedded server is needed: bin, lib, share.
REM Excluded: "pgAdmin 4" (723 MB GUI admin tool), data (default cluster - we run
REM initdb into our own pgdata), doc, include (headers), installer, scripts.
REM That drops the bundle from ~1 GB / 26,000 files to ~150 MB / ~5,000 files.
REM
REM robocopy, NOT xcopy: xcopy manages ~110 files/min here, robocopy /MT:16 does
REM ~9,000/min. robocopy skips matching files, so an interrupted run resumes cheaply.
REM Exit codes 0-7 are all SUCCESS for robocopy (8+ is a real failure).
robocopy "%PG_SRC%" "%PG_DST%" /E /MT:16 /NFL /NDL /NJH /NJS /NP /NC /R:1 /W:1 ^
    /XD "pgAdmin 4" data doc include installer scripts
if ERRORLEVEL 8 (
    echo [installer] ERROR: robocopy failed to copy PostgreSQL 18 to "%PG_DST%"
    goto :fail
)
echo [installer] PostgreSQL bundled at %PG_DST%

REM Ship a console launcher so failures are diagnosable on a customer machine:
REM ERPDesktop.exe uses javaw, so stdout goes nowhere.
copy /Y "%ROOT%.tools\debug-console.cmd" "%DIST%\%APP_NAME%\debug-console.cmd" >nul
echo [installer] debug launcher: %DIST%\%APP_NAME%\debug-console.cmd

REM =============================================================================
REM 4b. Wrap the app-image with Inno Setup
REM =============================================================================
echo.
echo === [4b/4] Building EXE installer (Inno Setup) ===
if not exist "%INNO_HOME%\ISCC.exe" (
    echo [installer] ERROR: Inno Setup not found at "%INNO_HOME%".
    goto :fail
)
REM Absolute paths are mandatory: ISCC resolves relative Source/OutputDir paths
REM against the .iss file's own directory (.tools\), not against %CD%.
"%INNO_HOME%\ISCC.exe" /Qp ^
    /DImageDir="%DIST%\%APP_NAME%" ^
    /DOutDir="%DIST%" ^
    "%ROOT%.tools\ERP-Desktop.iss"
if not "%ERRORLEVEL%"=="0" goto :fail

echo.
echo === Installer built ===
dir /b "%DIST%\*.exe"
exit /b 0

:fail
echo.
echo === Installer build FAILED ===
exit /b 1
