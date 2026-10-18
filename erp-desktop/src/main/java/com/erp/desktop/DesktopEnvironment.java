package com.erp.desktop;

import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/** Per-user configuration for the native executable as well as the script launcher. */
public final class DesktopEnvironment {
    private DesktopEnvironment() {}
    public static void configure() {
        System.clearProperty("erp.startup.error");
        try {
            String local = System.getenv("LOCALAPPDATA");
            Path profile = Path.of(local == null ? System.getProperty("user.home") : local, "ERP-Desktop");
            Path credential = profile.resolve("neon.credential.xml");
            String profileName = System.getProperty("quarkus.profile", "prod");
            boolean demo = java.util.Arrays.asList(profileName.split(",")).contains("demo");
            String demoUrl = demo ? System.getenv("ERP_DEMO_DATABASE_URL") : null;
            String demoJdbc = demo ? System.getenv("ERP_DEMO_JDBC_URL") : null;
            if (demoUrl != null && !demoUrl.isBlank()) System.setProperty("DATABASE_URL", demoUrl);
            else if (demoJdbc != null && !demoJdbc.isBlank()) {
                System.setProperty("quarkus.datasource.jdbc.url", demoJdbc);
                System.setProperty("DB_JDBC_URL", demoJdbc);
            }
            if (System.getenv("DATABASE_URL") == null && System.getenv("DB_JDBC_URL") == null
                    && System.getProperty("DATABASE_URL") == null && System.getProperty("DB_JDBC_URL") == null
                    && Files.exists(credential)) {
                ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-Command",
                        "$c = Import-Clixml -LiteralPath $env:ERP_CREDENTIAL_FILE; [Console]::Out.Write($c.GetNetworkCredential().Password)");
                builder.environment().put("ERP_CREDENTIAL_FILE", credential.toString());
                builder.redirectError(ProcessBuilder.Redirect.DISCARD);
                Process process = builder.start();
                if (!process.waitFor(20, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new IllegalStateException(); }
                String url = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                if (process.exitValue() != 0 || url.isBlank()) throw new IllegalStateException();
                System.setProperty("DATABASE_URL", url);
                System.setProperty("DB_BUNDLED_POSTGRES", "false");
            }
            String explicitKeys = System.getenv("ERP_KEY_DIR");
            Path keys = explicitKeys == null || explicitKeys.isBlank()
                    ? profile.resolve(demo ? "demo-keys" : "keys") : Path.of(explicitKeys);
            Files.createDirectories(keys);
            try (FileChannel channel = FileChannel.open(keys.resolve("keys.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 var lock = channel.lock()) {
                Path privateKey = keys.resolve("private.pem"), publicKey = keys.resolve("public.pem");
                if (!Files.exists(privateKey) && !Files.exists(publicKey)) {
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(3072); var pair = generator.generateKeyPair();
                    Files.writeString(privateKey, pem("PRIVATE KEY", pair.getPrivate().getEncoded()), StandardOpenOption.CREATE_NEW);
                    Files.writeString(publicKey, pem("PUBLIC KEY", pair.getPublic().getEncoded()), StandardOpenOption.CREATE_NEW);
                }
                if (!Files.exists(privateKey) || !Files.exists(publicKey)) throw new IllegalStateException();
                if (explicitKeys == null || explicitKeys.isBlank()) {
                    System.setProperty("mp.jwt.verify.publickey.location", com.erp.config.RuntimeConfigSource.keyLocation(publicKey));
                    System.setProperty("smallrye.jwt.sign.key.location", com.erp.config.RuntimeConfigSource.keyLocation(privateKey));
                }
            }
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
            System.setProperty("erp.startup.error", "The local connection profile could not be opened. Run Configure-Neon.ps1 under your Windows account, then restart.");
        }
    }
    private static String pem(String type, byte[] bytes) { return "-----BEGIN " + type + "-----\n" + Base64.getMimeEncoder(64,new byte[]{10}).encodeToString(bytes) + "\n-----END " + type + "-----\n"; }
}
