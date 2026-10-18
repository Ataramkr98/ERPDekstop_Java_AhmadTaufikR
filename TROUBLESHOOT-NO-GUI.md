# "The installed app starts but no window appears" — diagnosis & fixes

Applies to: `ERP-Desktop` (Quarkus 3.15.6 uber-jar + JavaFX 21 + jpackage app-image + Inno Setup)

---

## TL;DR — two independent causes, both required to fix it

There were **two** bugs stacked on top of each other. Fixing only one still leaves a
windowless app.

### Cause A — `Bootstrap` hangs on a PostgreSQL password prompt (never reaches JavaFX)

`com.erp.desktop.Bootstrap` is the process entry point; it starts the bundled PostgreSQL
before handing off to `Main` → `Application.launch(DesktopApp.class)`.

Port **5432 was already owned by the system-wide PostgreSQL 18**
(`C:\Program Files\PostgreSQL\18\bin\postgres.exe`). `Bootstrap` probed 5432, saw
something answering, concluded "server already running", skipped starting the bundled
server, then called `psql` / `createdb` **without `-w` and without `PGPASSWORD`** — so
they blocked **forever** on an interactive password prompt. The jpackage launcher is
`javaw`: no console, no stdin, nobody can ever answer.

Result: `Main.main()` never ran → no `Stage`, no window.

### Cause B — the custom jlink runtime is missing `java.net.http` (crashes *inside* JavaFX startup)

**This is the real "works from the IDE, not when packaged" bug.** From source you run on
the full JDK (all ~70 modules). The installer ships a `jlink` runtime built from an
incomplete `--add-modules` list — it contained **19 modules, and `java.net.http` was not
one of them**.

`com.erp.desktop.api.ApiClient` uses `java.net.http.HttpClient`, and it is constructed in
`DesktopApp.start()`. So the JavaFX toolkit *did* launch, then threw:

```
Exception in Application start method
Caused by: java.lang.NoClassDefFoundError: java/net/http/HttpClient
    at com.erp.desktop.api.ApiClient.<init>(ApiClient.java:35)
    at com.erp.desktop.DesktopApp.start(DesktopApp.java:46)
```

An exception in `Application.start()` kills the JavaFX launcher **before the first
`Stage` is shown** — so the process appears in Task Manager and then quietly dies, with
no window and (because it's `javaw`) no visible error. That is exactly the reported
symptom.

### What was NOT the cause (all checked and ruled out)

* JavaFX native libraries — all 59 `.dll` files (`glass.dll`, `prism_d3d.dll`,
  `javafx_font.dll`, `jfxmedia.dll`, `gstreamer-lite.dll`, …) are inside the uber-jar.
* JavaFX modules — `javafx.base/.graphics/.controls/.fxml/.media` **are** in the bundled
  runtime.
* Wrong main class — `app/ERP-Desktop.cfg` correctly says `com.erp.desktop.Bootstrap`.
* Missing FXML/CSS/images/i18n — the UI is built in code, there are no FXML or resource
  bundles to lose.
* `module-info.java` — does not exist by design; JavaFX runs on the classpath.

### Cause C — the desktop module does not ship the Flyway migrations (login 500s)

After Cause A and B are fixed the JavaFX sign-in window appears, but the login fails
with `The operation could not be completed. Reference: ERR-…` and every authenticated
endpoint returns **HTTP 500** with `relation "company" does not exist`.

The 13 Flyway migration files (`V1__platform.sql` … `V13__bi.sql`) live in
`erp-backend/src/main/resources/db/migration/`. They define all 119 tables.
`erp-desktop/pom.xml` only depends on **`erp-core`** (which holds the entities), **not**
on `erp-backend`. So the desktop uber-jar contained zero migrations; Flyway started,
found nothing to apply, and the `erp` database stayed empty.

Fix: move the migrations to `erp-core/src/main/resources/db/migration/`. Both
`erp-backend` and `erp-desktop` depend on `erp-core`, so both now ship them.

```bash
mv erp-backend/src/main/resources/db erp-core/src/main/resources/db
```

After the move, `unzip -l erp-desktop-…-runner.jar | grep db/migration/V` should
return 13 entries, and on first run Flyway applies them all (~30 s).

---

## All three fixes at a glance

| # | Symptom | Root cause | Fix |
|---|---|---|---|
| A | App hangs with no window; `pgdata/postmaster.pid` missing; port 5432 owned by system PostgreSQL. | `Bootstrap` called `psql` / `createdb` **without `-w`** and **without `PGPASSWORD`** against a password-protected foreign server, blocking forever on an interactive prompt. | Rewrite `Bootstrap` to use `-w`, set `PGPASSWORD`, bound every child with a timeout, fall back to port 55432, and **always** call `Main.main(args)`. |
| B | GUI window never appears even after A is fixed. Exception: `NoClassDefFoundError: java/net/http/HttpClient` in `DesktopApp.start()`. | The `jlink` runtime was built from an incomplete `--add-modules` list (19 modules, no `java.net.http`). From source the full JDK has every module. | Run `jdeps --print-module-deps` against the uber-jar, use that exact list in `build-installer.cmd`, and add a build-time guard that fails if `java.net.http` is missing. |
| C | Window appears, but login 500s with `relation "company" does not exist`. | The desktop uber-jar didn't include Flyway migrations (they were in `erp-backend`, which `erp-desktop` does not depend on). | Move `db/migration/` to `erp-core`. |

---

## Files to inspect (and what to look for)

| File | What to check |
|---|---|
| `erp-desktop/src/main/java/com/erp/desktop/Bootstrap.java` | The process entry point. Every child-process call must be bounded (`waitFor(timeout)`) and must pass `-w` (never prompt). |
| `erp-desktop/src/main/java/com/erp/desktop/Main.java` | Must **not** extend `Application`. It calls `Application.launch(DesktopApp.class, args)`. |
| `erp-desktop/src/main/java/com/erp/desktop/DesktopApp.java` | Boots Quarkus in-process, polls `/q/health`, shows the login scene. |
| `<install>/app/ERPDesktop.cfg` | `app.mainclass=` must be `com.erp.desktop.Bootstrap` (not `Main`, or PostgreSQL is never started). `app.classpath=$APPDIR\erp-desktop.jar`. |
| `erp-desktop/pom.xml` | `<quarkus.package.jar.type>uber-jar</quarkus.package.jar.type>`; OpenJFX deps must use the `-win` classifier (`<javafx.platform>win</javafx.platform>`). |
| `build-installer.cmd` | `--main-class`, `--input` (must be a dir with **only** the jar), `--runtime-image`. |
| `.tools/ERP-Desktop.iss` | Inno Setup wrapper around the jpackage app-image. |
| `erp-desktop/src/main/resources/application.properties` | `quarkus.http.port`, datasource `${DB_HOST}/${DB_PORT}` placeholders. |
| `module-info.java` | **Does not exist**, by design. JavaFX is loaded from the **classpath**, not the module path — so there is no module descriptor to get wrong. |

---

## Logs & console output to capture

1. **`<install-root>/logs/erp-desktop.log`** — `Bootstrap.say()` mirrors every step here,
   because stdout is invisible when launched from the `.exe` (`javaw` has no console).
   This is the single most useful artifact.
2. **`<install-root>/pgdata/pg.log`** — `pg_ctl -l` output for the bundled cluster.
3. **Console output** — run the bundled runtime's `java.exe` (not `javaw`) so stdout is
   visible:
   ```bat
   cd "E:\my project\javaspring\erp-dekstop-app\erp-desktop\target\_installtest"
   runtime\bin\java.exe -Djava.home="%CD%\runtime" -cp app\erp-desktop.jar com.erp.desktop.Bootstrap
   ```
   Or build a console-enabled image with `jpackage --win-console`.
4. **`pgdata/postmaster.pid`** — its presence (and line 4, the port) tells you whether the
   *bundled* server is actually up.

---

## Commands that confirm the root cause

```bash
# 0. Is anything on 5432? Who owns it?
netstat -ano | grep ":5432"
#    -> LISTENING, PID 4072. Then:
ps -W | grep 4072
#    -> /c/Program Files/PostgreSQL/18/bin/postgres.exe   <-- SYSTEM server, not ours

# 1. Is the BUNDLED server running? (from the install dir)
PostgreSQL\18\bin\pg_ctl.exe status -D "%CD%\pgdata"
#    -> "pg_ctl: no server running"  => Bootstrap never started it

# 2. The decisive test — does psql hang?
PostgreSQL\18\bin\psql.exe -h localhost -p 5432 -U postgres -d postgres -tAc "SELECT 1"
#    -> HANGS FOREVER (exit 124 under `timeout`)  => waiting on a password prompt

# 3. Prove it's the password prompt:
PostgreSQL\18\bin\psql.exe -w -h localhost -p 5432 -U postgres -d postgres -tAc "SELECT 1"
#    -> immediate: "fe_sendauth: no password supplied"
set PGPASSWORD=admin
PostgreSQL\18\bin\psql.exe -h localhost -p 5432 -U postgres -d postgres -tAc "SELECT 1"
#    -> immediate: 1

# 4. Prove the packaged app never reaches JavaFX:
timeout 120 runtime\bin\java.exe -Djava.home="%CD%\runtime" -cp app\erp-desktop.jar com.erp.desktop.Bootstrap
#    OLD: stops at "[erp] creating database 'erp'..." then exit 124
#    NEW: "[erp] port 5432 is already served by another PostgreSQL - using 55432 ..."
#         "[erp] database 'erp' created"
#         "[erp] using bundled PostgreSQL on port 55432"
#         -> reaches Main -> GUI
```

### Ruling out the JavaFX suspects

```bash
# JavaFX native libraries present inside the uber-jar?
unzip -l erp-desktop\target\erp-desktop-1.0.0-SNAPSHOT-runner.jar | grep -E "\.dll$" | wc -l
#    -> 59    (glass.dll, prism_d3d.dll, javafx_font.dll, javafx_iio.dll,
#              decora_sse.dll, prism_sw.dll, jfxmedia.dll, gstreamer-lite.dll ...)

# What modules are in the bundled runtime?
type _installtest\runtime\release
# or: _installtest\runtime\bin\java.exe --list-modules | find "javafx"
```

The bundled runtime contains **no `javafx.*` modules** — that is fine and expected:
jlink cannot link OpenJFX automatic modules from Maven Central
(`org.openjfx:javafx-jmods` is not published there), so JavaFX is loaded from the
uber-jar classpath. Do not "fix" this by pointing jlink at the jars.

---

## The fixes applied

### 1. `Bootstrap.java` — never adopt a foreign server, never prompt, never hang

* `PG_PORT_PREFERRED = 5432`, `PG_PORT_FALLBACK = 55432`.
  `choosePort()` reads `pgdata/postmaster.pid` line 4 to find the port of *our* cluster;
  if 5432 is answered by any other PostgreSQL, the bundled server starts on **55432**.
* `psql` and `createdb` are always called with **`-w`** (never issue a password prompt)
  and with **`PGPASSWORD` injected into the child environment**.
* Every child process is bounded: `p.waitFor(CHILD_TIMEOUT_SECONDS=90, SECONDS)` plus
  `destroyForcibly()` on timeout, and stdout is drained so a full pipe can't deadlock.
* A hard `SETUP_TIMEOUT_SECONDS = 120` deadline stops the whole bootstrap.
* `say()` writes progress to stdout **and** `<install-root>/logs/erp-desktop.log`.
* `main()` is wrapped in `try/catch (Throwable)` so **`Main.main(args)` always runs** —
  even a total PostgreSQL failure now shows the GUI (with the existing boot-failure /
  retry screen) instead of a silent no-op.
* On success it sets `DB_HOST` / `DB_PORT` as **system properties** (ordinal 400), which
  outrank environment variables (300) and the `${DB_PORT:5432}` default in
  `application.properties`.

### 2. `build-installer.cmd`

* `--main-class com.erp.desktop.Bootstrap` (was accidentally `...Main`, which skipped the
  database entirely).
* `--input` now points at a staging dir `.tools\appinput` containing **only** the jar.
  It previously pointed at `erp-desktop\target`, so jpackage recursively copied
  `target\_installtest` (a complete prior install with its own `runtime\` and
  `PostgreSQL\`) and `target\installer` into itself — tens of thousands of files and an
  apparent hang.
* Dropped `--verbose` (unreadable output) and corrected the stale header comment claiming
  jpackage `--type exe` uses Inno Setup — it requires WiX, which is why the pipeline is
  `app-image` → `ISCC.exe`.

---

## If it still fails — triage order

1. Read `<install-root>\logs\erp-desktop.log`. Whatever the last `[erp]` line is, that is
   where it stopped.
2. `pg_ctl status -D "<install-root>\pgdata"` — if "no server running", the port logic is
   still wrong; check `postmaster.pid`.
3. Re-run with `runtime\bin\java.exe` (console) to see the Quarkus banner. If Quarkus
   starts but the window doesn't, the problem really is JavaFX — then check
   `DesktopApp.start()` for a swallowed exception and add a
   `Thread.setDefaultUncaughtExceptionHandler` that writes to the log.
4. Confirm the log-in credentials: `admin` / `admin`
   (`quarkus.security.users.embedded.*`).

---

## Rebuild

```bat
mvnw.cmd -pl erp-desktop -am -DskipTests package
build-installer.cmd
```

Outputs:
* `dist\ERP-Desktop\` — the jpackage app-image (with `PostgreSQL\18` bundled in)
* `dist\ERP-Desktop-1.0.0-setup.exe` — the Inno Setup installer
