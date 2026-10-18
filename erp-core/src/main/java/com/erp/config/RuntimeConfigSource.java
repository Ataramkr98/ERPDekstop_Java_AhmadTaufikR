package com.erp.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.microprofile.config.spi.ConfigSource;

/** Converts hosted PostgreSQL URIs without ever logging their credentials. */
public final class RuntimeConfigSource implements ConfigSource {
    private final Map<String, String> values = new LinkedHashMap<>();
    public RuntimeConfigSource() {
        // Demo and normal profiles share the same PostgreSQL system of record: the
        // demo is isolated by tenant (company code DEMO), not by database engine.
        // quarkus.datasource.db-kind is build-time configuration in Quarkus, so a
        // runtime H2 fallback here would never take effect and is intentionally
        // absent. Use ERP_DEMO_DATABASE_URL / ERP_DEMO_JDBC_URL for a separate
        // demo database, or point DATABASE_URL at a demo database.
        String url = firstNonBlank(
                System.getProperty("DATABASE_URL"),
                System.getenv("DATABASE_URL"),
                System.getProperty("ERP_DEMO_DATABASE_URL"),
                System.getenv("ERP_DEMO_DATABASE_URL"));
        if (url != null && !url.isBlank()) values.putAll(databaseProperties(url));
        String keyDir = System.getenv("ERP_KEY_DIR");
        if (keyDir != null && !keyDir.isBlank()) {
            values.put("mp.jwt.verify.publickey.location", keyLocation(Path.of(keyDir, "public.pem")));
            values.put("smallrye.jwt.sign.key.location", keyLocation(Path.of(keyDir, "private.pem")));
        }
    }
    public static Map<String, String> databaseProperties(String input) {
        try {
            URI uri = URI.create(input.replace("\\_", "_").replace("\\@", "@"));
            if (!("postgresql".equals(uri.getScheme()) || "postgres".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawUserInfo() == null
                    || uri.getPath() == null || uri.getPath().length() < 2) throw new IllegalArgumentException();
            String[] credentials = uri.getRawUserInfo().split(":", 2);
            if (credentials.length != 2) throw new IllegalArgumentException();
            Map<String, String> query = new LinkedHashMap<>();
            if (uri.getRawQuery() != null) for (String part : uri.getRawQuery().split("&")) {
                String[] pair = part.split("=", 2);
                String key = decode(pair[0]);
                if (key.equals("channel_binding")) key = "channelBinding";
                query.put(key, pair.length == 2 ? decode(pair[1]) : "");
            }
            if (uri.getHost().endsWith(".neon.tech")) {
                query.putIfAbsent("sslmode", "require");
                query.putIfAbsent("channelBinding", "require");
            }
            String params = query.entrySet().stream().map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                    .collect(java.util.stream.Collectors.joining("&"));
            String host = uri.getHost();
            if (host.contains(":") && !host.startsWith("[")) host = "[" + host + "]";
            return Map.of("quarkus.datasource.jdbc.url", "jdbc:postgresql://" + host + ":"
                            + (uri.getPort() < 0 ? 5432 : uri.getPort()) + uri.getRawPath()
                            + (params.isBlank() ? "" : "?" + params),
                    "quarkus.datasource.username", decode(credentials[0]),
                    "quarkus.datasource.password", decode(credentials[1]));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("DATABASE_URL must be a PostgreSQL connection URI with a host, database and credentials.");
        }
    }

    /** SmallRye's file loader reads filenames directly; it does not URL-decode file URIs. */
    public static String keyLocation(Path path) {
        return path.toAbsolutePath().normalize().toString();
    }
    private static String decode(String text) { return URLDecoder.decode(text.replace("+", "%2B"), StandardCharsets.UTF_8); }
    private static String encode(String text) { return java.net.URLEncoder.encode(text, StandardCharsets.UTF_8); }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }
    @Override public Map<String, String> getProperties() { return Map.copyOf(values); }
    @Override public Set<String> getPropertyNames() { return values.keySet(); }
    @Override public String getValue(String name) { return values.get(name); }
    @Override public String getName() { return "ERP runtime environment"; }
    @Override public int getOrdinal() { return 290; }
}
