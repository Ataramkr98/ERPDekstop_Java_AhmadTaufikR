package com.erp;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResource;

/**
 * Backend test profile with the demo workspace switched on.
 *
 * <p>Shares the H2 database and ephemeral keys of {@link BackendTestResource}; the only
 * difference is {@code erp.demo.enabled}, which is what a real deployment would set via
 * {@code -Dquarkus.profile=demo}.
 */
public class DemoProfile extends BackendProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> config = new HashMap<>(super.getConfigOverrides());
        config.put("erp.demo.enabled", "true");
        config.put("erp.demo.company-code", "DEMO");
        config.put("erp.demo.username", "demo");
        return config;
    }
}