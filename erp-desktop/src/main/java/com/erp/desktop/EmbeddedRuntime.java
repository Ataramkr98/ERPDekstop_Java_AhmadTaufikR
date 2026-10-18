package com.erp.desktop;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import jakarta.enterprise.context.ApplicationScoped;

/** Keeps the embedded REST service alive until the JavaFX application requests exit. */
@ApplicationScoped
public class EmbeddedRuntime implements QuarkusApplication {
    @Override
    public int run(String... args) {
        Quarkus.waitForExit();
        return 0;
    }
}
