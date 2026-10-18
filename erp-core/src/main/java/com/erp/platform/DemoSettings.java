package com.erp.platform;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Runtime switch for the portfolio demo workspace.
 *
 * <p>Demo mode issues a session without a password and creates a dedicated demo
 * tenant, so it must never be reachable on a real deployment. The gate is a plain
 * runtime property that defaults to {@code false} and is only turned on by the demo
 * launcher ({@code run-demo.cmd} / {@code scripts/Start-Erp.ps1 -Mode Demo}), which
 * also points the application at a demo database. {@link DemoSettings#enabled()} is
 * additionally forced off whenever the Quarkus profile is a production profile, so a
 * stray {@code -Derp.demo.enabled=true} on a prod deployment still cannot enable it.
 */
@ApplicationScoped
public class DemoSettings {

    @ConfigProperty(name = "erp.demo.enabled", defaultValue = "false")
    boolean configured;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String quarkusProfile;

    @ConfigProperty(name = "erp.demo.company-code", defaultValue = "DEMO")
    String companyCode;

    @ConfigProperty(name = "erp.demo.username", defaultValue = "demo")
    String username;

    /** True only when demo mode was explicitly enabled and the profile is not production. */
    public boolean enabled() {
        return configured && !isProductionProfile();
    }

    private boolean isProductionProfile() {
        String profile = quarkusProfile == null ? "" : quarkusProfile.trim().toLowerCase();
        return profile.startsWith("prod") || profile.startsWith("live");
    }

    public String companyCode() {
        return companyCode;
    }

    public String username() {
        return username;
    }
}