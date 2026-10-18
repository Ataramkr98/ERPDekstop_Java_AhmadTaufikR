package com.erp.desktop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Process entry point for the packaged desktop application.
 *
 * <p>Before the JavaFX shell (and the in-process Quarkus core it starts) comes
 * up, this brings up the <em>bundled</em> PostgreSQL instance so the product is
 * fully self-contained: it initializes the cluster once, starts the server, and
 * makes sure the {@code erp} database exists.
 *
 * <p><b>Contract (important):</b> this method must <em>always</em> hand control
 * to {@link Main}. A database problem has to surface as DesktopApp's own
 * "PostgreSQL must be running" retry screen - never as an invisible process.
 * Every child process is therefore bounded by a timeout, the whole bring-up has
 * a hard deadline, and stdout/stderr are mirrored to a log file because the
 * packaged {@code .exe} has no console.
 *
 * <p>Layout assumption (matched by the jpackage / Inno Setup packaging): the
 * bundled PostgreSQL binaries live at {@code <install-root>/PostgreSQL/18} and
 * {@code java.home} is the bundled runtime ({@code <install-root>/runtime}), so
 * the install root is its parent.
 *
 * <p>The <em>data directory</em> deliberately does not live under the install
 * root. {@code C:\Program Files} is not a writable location for runtime data:
 * initdb cannot set the directory permissions it needs there and aborts with
 * "could not change permissions of directory ... Permission denied", leaving an
 * empty cluster behind. The cluster therefore goes to
 * {@code %LOCALAPPDATA%\ERP-Desktop\pgdata}, which needs no elevation.
 */
public final class Bootstrap {

    private static final String PG_DIR = "PostgreSQL/18";
    private static final String PGDATA = "pgdata";
    /** Per-user data root; keeps the cluster out of C:\Program Files. */
    private static final String APP_DATA_DIR = "ERP-Desktop";

    /**
     * Preferred port for the bundled server.
     *
     * <p>This used to be the only port, which broke the app on any machine that
     * already runs a system-wide PostgreSQL on 5432: Bootstrap saw a foreign
     * server answering, assumed it was the bundled one, never started its own,
     * and then blocked forever on that server's interactive password prompt -
     * so Main.main() never ran and no window ever appeared.
     */
    private static final int PG_PORT_PREFERRED = 5432;
    /** Used when the preferred port is already served by a foreign server. */
    private static final int PG_PORT_FALLBACK = 55432;

    private static final String DB_NAME = "erp";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin";

    /** Hard ceiling for the entire DB bring-up. The UI must appear regardless. */
    private static final long SETUP_TIMEOUT_SECONDS = 120L;
    /** Ceiling for a single child process (initdb is the slow one, ~30s). */
    private static final long CHILD_TIMEOUT_SECONDS = 90L;
    /** How long to wait for the server to accept connections after pg_ctl start. */
    private static final int READY_POLL_SECONDS = 30;
    // Number of attempts to bring the bundled PostgreSQL up before giving up. The
    // very first spawn on Windows occasionally fails with "Access is denied" while
    // a previous instance is releasing the data directory; a short pause plus one
    // retry clears it.
    private static final int START_ATTEMPTS = 3;
    private static final long START_RETRY_PAUSE_MILLIS = 2_000L;

    private static Path logFile;

    private Bootstrap() {
    }

    public static void main(String[] args) {
        DesktopEnvironment.configure();
        long deadline = System.currentTimeMillis() + SETUP_TIMEOUT_SECONDS * 1000L;
        try {
            int port = startPostgres(deadline);
            if (port > 0) {
                // Tell the Quarkus core which port the bundled server ended up on.
                // application.properties reads ${DB_PORT:5432}; system properties
                // (ordinal 400) outrank environment variables (ordinal 300).
                System.setProperty("DB_HOST", "localhost");
                System.setProperty("DB_PORT", String.valueOf(port));
                say("using bundled PostgreSQL on port " + port);
            }
        } catch (Throwable t) {
            // Never let a bootstrap failure keep the window from appearing.
            say("bootstrap error: " + t);
        }
        Main.main(args);
    }

    /**
     * @return the port the bundled server is reachable on, or 0 to leave the
     *         datasource configuration untouched (external server mode).
     */
    private static int startPostgres(long deadline) {
        Path installRoot = installRoot();
        Path pgHome = installRoot.resolve(PG_DIR);
        Path pgBin = pgHome.resolve("bin");
        Path pgData = dataDir(installRoot);

        initLog(installRoot);

        // Skip bundled PostgreSQL when the user has configured a remote / hosted
        // database (e.g. Neon). Setting DB_JDBC_URL or DB_BUNDLED_POSTGRES=false
        // turns this app into a pure client of whatever database application.properties
        // resolves to.
        if (bundledDisabled()) {
            String why = System.getProperty("DB_JDBC_URL");
            if (why == null || why.isBlank()) {
                why = System.getenv("DB_JDBC_URL");
            }
            say("bundled PostgreSQL disabled - using the configured remote database.");
            return 0;
        }

        Path initdb = pgBin.resolve("initdb.exe");
        Path pgCtl = pgBin.resolve("pg_ctl.exe");
        Path pgIsReady = pgBin.resolve("pg_isready.exe");
        Path psql = pgBin.resolve("psql.exe");
        Path createdb = pgBin.resolve("createdb.exe");

        if (!Files.exists(initdb) || !Files.exists(pgCtl)) {
            say("bundled PostgreSQL not found at " + pgHome
                    + " - falling back to an external server.");
            return 0;
        }

        try {
            // 1. Initialize the cluster on first run only.
            if (!Files.exists(pgData.resolve("PG_VERSION"))) {
                if (expired(deadline)) {
                    say("setup deadline reached before initdb - skipping DB bring-up.");
                    return 0;
                }
                // Only the parent is created: initdb insists on creating the data
                // directory itself so it can apply the restrictive ACLs it needs.
                // Pre-creating it (even empty) makes initdb fall back to "fixing
                // permissions on existing directory", which fails on locked-down
                // locations and on some ACLs.
                Files.createDirectories(pgData.getParent());
                say("initializing bundled PostgreSQL data directory at "
                        + pgData + " (first run)...");
                int rc = run(List.of(initdb.toString(), "-D", pgData.toString(),
                        "-U", DB_USER, "-A", "trust", "-E", "UTF8"), pgBin);
                if (rc != 0) {
                    say("initdb failed (rc=" + rc + ")");
                    return 0;
                }
            }

            // 2. Pick a port, then make sure OUR server is running there.
            int port = choosePort(pgCtl, pgIsReady, pgData, deadline);
            if (port <= 0) {
                say("could not start the bundled PostgreSQL server.");
                return 0;
            }

            // 3. Create the application database if it is missing.
            if (!expired(deadline)) {
                ensureDatabase(psql, createdb, pgBin, port);
            }
            return port;
        } catch (IOException e) {
            say("I/O error during PostgreSQL bring-up: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Decide which port the bundled server owns.
     *
     * <p>We must never adopt a foreign server: a port is only reused when our
     * own cluster is the one answering there (or when nothing answers at all).
     */
    private static int choosePort(Path pgCtl, Path pgIsReady, Path pgData, long deadline)
            throws IOException {
        // Already running? Read the port our cluster recorded in postmaster.pid.
        int recorded = recordedPort(pgData);
        if (recorded > 0 && isReady(pgIsReady, recorded)) {
            say("bundled PostgreSQL already running on port " + recorded);
            return recorded;
        }

        // Not running (or stale pid file). Take the preferred port if it is free,
        // otherwise keep our instance separate on the fallback port.
        int candidate = isReady(pgIsReady, PG_PORT_PREFERRED) ? PG_PORT_FALLBACK : PG_PORT_PREFERRED;
        if (candidate == PG_PORT_FALLBACK) {
            say("port " + PG_PORT_PREFERRED
                    + " is already served by another PostgreSQL - using " + PG_PORT_FALLBACK
                    + " for the bundled instance.");
        }

        if (expired(deadline)) {
            say("setup deadline reached before pg_ctl start - skipping DB bring-up.");
            return 0;
        }

        for (int attempt = 1; attempt <= START_ATTEMPTS; attempt++) {
            say("starting bundled PostgreSQL on port " + candidate
                    + (attempt > 1 ? " (attempt " + attempt + " of " + START_ATTEMPTS + ")" : "")
                    + "...");
            // Deliberately no "-w": pg_ctl's own readiness wait has been observed
            // to hang well past its -t timeout on Windows, and without it the exit
            // code says little. We spawn and poll pg_isready ourselves, which gives
            // a bounded wait and lets us surface pg.log when startup fails.
            int rc = run(List.of(pgCtl.toString(), "-D", pgData.toString(),
                    "-l", pgData.resolve("pg.log").toString(),
                    "-o", "-p " + candidate, "start"), pgBin(pgCtl));
            if (rc != 0) {
                say("pg_ctl start returned rc=" + rc);
            }

            for (int i = 0; i < READY_POLL_SECONDS && !isReady(pgIsReady, candidate); i++) {
                if (expired(deadline)) {
                    break;
                }
                sleep(1000L);
            }
            if (isReady(pgIsReady, candidate)) {
                return candidate;
            }
            if (attempt < START_ATTEMPTS) {
                // Windows sporadically refuses the very first spawn with "Access is
                // denied", usually while a previous instance is still releasing the
                // data directory. A short pause plus one retry clears it far more
                // often than it recurs.
                say("server did not come up - retrying after a short pause");
                sleep(START_RETRY_PAUSE_MILLIS);
            }
        }
        say("bundled PostgreSQL did not become ready on port " + candidate);
        dumpPgLog(pgData);
        return 0;
    }

    /** Creates the {@code erp} database if it does not exist yet. */
    private static void ensureDatabase(Path psql, Path createdb, Path pgBin, int port) {
        // -w is essential: without it psql/createdb block forever on an
        // interactive password prompt when the server requires authentication
        // and there is no console attached (which is exactly how the packaged
        // .exe runs). The cluster is initialised with "-A trust", but keep the
        // belt-and-braces PGPASSWORD for a hardened/hand-edited cluster.
        List<String> check = List.of(psql.toString(), "-w",
                "-h", "localhost", "-p", String.valueOf(port),
                "-U", DB_USER, "-d", "postgres",
                "-tAc", "SELECT 1 FROM pg_database WHERE datname='" + DB_NAME + "'");
        String out = runCapture(check, pgBin);
        if (out != null && out.contains("1")) {
            say("database '" + DB_NAME + "' already present");
            return;
        }
        say("creating database '" + DB_NAME + "'...");
        int rc = run(List.of(createdb.toString(), "-w",
                "-h", "localhost", "-p", String.valueOf(port),
                "-U", DB_USER, DB_NAME), pgBin);
        say(rc == 0 ? "database '" + DB_NAME + "' created"
                    : "createdb failed (rc=" + rc + ")");
    }

    // ------------------------------------------------------------------ helpers

    /** Port our cluster is listening on, as recorded by the postmaster, or -1. */
    private static int recordedPort(Path pgData) {
        Path pid = pgData.resolve("postmaster.pid");
        if (!Files.exists(pid)) {
            return -1;
        }
        try {
            List<String> lines = Files.readAllLines(pid, StandardCharsets.UTF_8);
            // postmaster.pid: 1=PID, 2=data dir, 3=start time, 4=port, ...
            if (lines.size() >= 4) {
                return Integer.parseInt(lines.get(3).trim());
            }
        } catch (IOException | NumberFormatException ignored) {
            // Stale or truncated pid file - treat as "not running".
        }
        return -1;
    }

    private static boolean isReady(Path pgIsReady, int port) {
        return runQuiet(List.of(pgIsReady.toString(),
                "-h", "localhost", "-p", String.valueOf(port))) == 0;
    }

    private static Path pgBin(Path pgCtl) {
        return pgCtl.getParent();
    }

    /**
     * Where the cluster lives.
     *
     * <p>Per-user and writable by construction: {@code %LOCALAPPDATA%\ERP-Desktop\pgdata}.
     * A cluster previously created in the install root (pre-fix layout) is still
     * honoured so an existing installation does not lose its data.
     */
    private static Path dataDir(Path installRoot) {
        Path preferred = userDataRoot().resolve(PGDATA);
        Path legacy = installRoot.resolve(PGDATA);
        if (Files.exists(legacy.resolve("PG_VERSION")) && !Files.exists(preferred.resolve("PG_VERSION"))) {
            say("reusing cluster found in the install root at " + legacy);
            return legacy;
        }
        return preferred;
    }

    /** {@code %LOCALAPPDATA%\ERP-Desktop} (fallback: {@code ~/AppData/Local/ERP-Desktop}). */
    private static Path userDataRoot() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = (localAppData != null && !localAppData.isBlank())
                ? Path.of(localAppData)
                : Path.of(System.getProperty("user.home", "."), "AppData", "Local");
        return base.resolve(APP_DATA_DIR);
    }

    private static boolean expired(long deadline) {
        return System.currentTimeMillis() > deadline;
    }

    /**
     * True when the user has asked us NOT to start a bundled PostgreSQL — typically
     * because they configured a remote database (Neon, RDS, ...) via DB_JDBC_URL or
     * because they want the app to talk to an existing PostgreSQL on this machine.
     * System properties outrank environment variables.
     */
    private static boolean bundledDisabled() {
        if (System.getProperty("DATABASE_URL") != null) return true;
        if (System.getenv("DATABASE_URL") != null) return true;
        if (hasText("DB_JDBC_URL")) {
            return true;
        }
        String flag = firstNonBlank(
                System.getProperty("DB_BUNDLED_POSTGRES"),
                System.getenv("DB_BUNDLED_POSTGRES"));
        if (flag != null) {
            String f = flag.trim().toLowerCase();
            return f.equals("false") || f.equals("0") || f.equals("no") || f.equals("off");
        }
        return false;
    }

    private static boolean hasText(String name) {
        String v = firstNonBlank(System.getProperty(name), System.getenv(name));
        return v != null && !v.isBlank();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        return (b != null && !b.isBlank()) ? b : null;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Copies the tail of the cluster log into our own log. When postgres refuses
     * to start this is the only place the reason appears.
     */
    private static void dumpPgLog(Path pgData) {
        Path pgLog = pgData.resolve("pg.log");
        if (!Files.exists(pgLog)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(pgLog, StandardCharsets.UTF_8);
            int from = Math.max(0, lines.size() - 30);
            say("---- pg.log (last " + (lines.size() - from) + " lines) ----");
            for (int i = from; i < lines.size(); i++) {
                say("    | " + lines.get(i));
            }
        } catch (IOException ignored) {
            // Logging must never break startup.
        }
    }

    /** Appends raw child-process output to the log so failures are explainable. */
    private static void logOutput(String output) {
        for (String line : output.split("\\R")) {
            if (!line.isBlank()) {
                say("    | " + line);
            }
        }
    }

    /** Runs a child process, bounding it so it can never hang the UI forever. */
    private static int run(List<String> command, Path prependPath) {
        return exec(command, prependPath, false);
    }

    private static int runQuiet(List<String> command) {
        return exec(command, null, true);
    }

    private static int exec(List<String> command, Path prependPath, boolean quiet) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (prependPath != null) {
                pb.environment().put("PATH", prependPath + ";" + System.getenv("PATH"));
            }
            pb.environment().put("PGPASSWORD", DB_PASSWORD);
            if (!quiet) {
                pb.redirectErrorStream(true);
            } else {
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            }
            Process p = pb.start();
            StringBuilder sink = new StringBuilder();
            Thread drainer = null;
            if (!quiet) {
                // Drain into a buffer rather than to stdout: the child must not
                // block on a full pipe, and the packaged .exe has no console, so
                // anything written only to stdout is lost - it has to reach the
                // log file or a failure like "initdb failed (rc=1)" is undiagnosable.
                drainer = new Thread(() -> {
                    try (var in = p.getInputStream()) {
                        sink.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                    } catch (IOException ignored) {
                        // Child closed its stream - nothing to do.
                    }
                });
                drainer.start();
            }
            boolean finished = p.waitFor(CHILD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                say("timeout: " + command.get(0) + " - terminating");
                p.destroyForcibly();
            }
            if (drainer != null) {
                drainer.join(2_000L);
            }
            if (!quiet && sink.length() > 0) {
                logOutput(sink.toString());
            }
            return finished ? p.exitValue() : -1;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            say("could not run " + command.get(0) + ": " + e.getMessage());
            return -1;
        }
    }

    private static String runCapture(List<String> command, Path prependPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (prependPath != null) {
                pb.environment().put("PATH", prependPath + ";" + System.getenv("PATH"));
            }
            pb.environment().put("PGPASSWORD", DB_PASSWORD);
            // Errors must land in the captured stream too, otherwise a failed
            // psql is indistinguishable from "database does not exist".
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String out;
            try (var in = p.getInputStream()) {
                out = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (!p.waitFor(CHILD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                say("timeout: " + command.get(0) + " - terminating");
                p.destroyForcibly();
                return null;
            }
            if (out != null && out.contains("error") && !out.contains(DB_NAME)) {
                logOutput(out);
            }
            return out;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            say("could not run " + command.get(0) + ": " + e.getMessage());
            return null;
        }
    }

    private static Path installRoot() {
        // java.home -> .../ERPDesktop/runtime  =>  parent is the install root.
        return Path.of(System.getProperty("java.home")).getParent();
    }

    // --------------------------------------------------------------------- log

    private static void initLog(Path installRoot) {
        // Prefer the per-user root: <install>\logs is not writable under
        // C:\Program Files, and a log that cannot be written is useless.
        Path[] candidates = {
                userDataRoot().resolve("logs").resolve("erp-desktop.log"),
                installRoot.resolve("logs").resolve("erp-desktop.log"),
        };
        for (Path candidate : candidates) {
            try {
                Files.createDirectories(candidate.getParent());
                Files.writeString(candidate, "", StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                logFile = candidate;
                return;
            } catch (IOException ignored) {
                // Try the next location.
            }
        }
        logFile = null;
    }

    /**
     * Mirrors progress to stdout and to {@code <install>/logs/erp-desktop.log}.
     * stdout is invisible when launched from the GUI .exe, so without the file
     * there is no way to diagnose a bad install.
     */
    private static void say(String message) {
        String line = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " [erp] " + message;
        System.out.println(line);
        if (logFile != null) {
            try {
                Files.writeString(logFile, line + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ignored) {
                // Logging must never break startup.
            }
        }
    }
}
