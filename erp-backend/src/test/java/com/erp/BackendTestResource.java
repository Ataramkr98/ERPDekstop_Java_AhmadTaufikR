package com.erp;

import java.nio.file.*;
import java.security.*;
import java.util.*;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/** Isolated H2 database and ephemeral signing keys; never uses Neon credentials. */
public class BackendTestResource implements QuarkusTestResourceLifecycleManager {
    private Path directory;
    @Override public Map<String,String> start() {
        try {
            directory=Files.createTempDirectory("erp-test-keys-");
            KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA"); generator.initialize(2048); KeyPair pair=generator.generateKeyPair();
            Files.writeString(directory.resolve("private.pem"),pem("PRIVATE KEY",pair.getPrivate().getEncoded()));
            Files.writeString(directory.resolve("public.pem"),pem("PUBLIC KEY",pair.getPublic().getEncoded()));
            return Map.of("quarkus.datasource.db-kind","h2", "quarkus.datasource.jdbc.url","jdbc:h2:mem:erp_tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE",
                    "quarkus.datasource.username","sa","quarkus.datasource.password","", "quarkus.datasource.devservices.enabled","false",
                    "mp.jwt.verify.publickey.location",com.erp.config.RuntimeConfigSource.keyLocation(directory.resolve("public.pem")),
                    "smallrye.jwt.sign.key.location",com.erp.config.RuntimeConfigSource.keyLocation(directory.resolve("private.pem")));
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    private static String pem(String type, byte[] value) { return "-----BEGIN " + type + "-----\n" + Base64.getMimeEncoder(64,new byte[]{10}).encodeToString(value) + "\n-----END " + type + "-----\n"; }
    @Override public void stop() {
        if (directory != null) try { Files.deleteIfExists(directory.resolve("private.pem")); Files.deleteIfExists(directory.resolve("public.pem")); Files.deleteIfExists(directory); } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
}
