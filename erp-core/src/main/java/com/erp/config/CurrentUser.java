package com.erp.config;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Identity and tenancy of the caller.
 *
 * <p>JWT claims carry {@code companyId} so every query can be scoped to the active
 * legal entity (PRD M-ADM-003). Outside a request - schedulers, Flyway seeding, JPA
 * callbacks during startup - the accessors degrade to {@code system} rather than exploding.
 */
@ApplicationScoped
public class CurrentUser {

    public static final String SYSTEM = "system";
    private static final String CLAIM_COMPANY = "companyId";

    @Inject
    Instance<JsonWebToken> jwtInstance;

    public String username() {
        JsonWebToken jwt = token();
        if (jwt == null) {
            return SYSTEM;
        }
        String name = jwt.getName();
        return (name == null || name.isBlank()) ? SYSTEM : name;
    }

    public Long companyId() {
        JsonWebToken jwt = token();
        if (jwt == null) {
            return null;
        }
        Object claim = jwt.getClaim(CLAIM_COMPANY);
        if (claim == null) {
            return null;
        }
        if (claim instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.valueOf(claim.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Set<String> roles() {
        JsonWebToken jwt = token();
        if (jwt == null) {
            return Set.of();
        }
        Set<String> groups = jwt.getGroups();
        return groups == null ? Set.of() : new HashSet<>(groups);
    }

    public boolean hasRole(String role) {
        return roles().contains(role);
    }

    /**
     * True when this is a portfolio demo session. Set as a JWT claim by the session
     * issuer; it labels the session and gates the demo-only endpoints. It grants no
     * extra authority beyond the demo tenant the token is already scoped to.
     */
    public boolean isDemo() {
        return booleanClaim(token(), "demo");
    }

    /**
     * Reads a boolean claim without assuming its Java type.
     *
     * <p>SmallRye's {@code DefaultJWTCallerPrincipal.fixJoseTypes()} replaces the value of
     * every <em>custom</em> claim with a JSON-P {@code JsonValue}, so a JSON {@code true}
     * arrives as {@code JsonValue.TRUE} rather than {@code java.lang.Boolean}. A strict
     * {@code Boolean.TRUE.equals(claim)} therefore returns false for a claim that is
     * present and true. (The numeric {@code companyId} claim escaped the same trap only
     * because {@code JsonNumber} implements {@link Number}.) Both shapes are accepted
     * here, so the check cannot silently fail again if the library changes its mind.
     */
    public static boolean booleanClaim(JsonWebToken jwt, String name) {
        if (jwt == null) {
            return false;
        }
        Object claim = jwt.getClaim(name);
        if (claim instanceof Boolean flag) {
            return flag;
        }
        return claim != null && "true".equals(String.valueOf(claim));
    }

    /** Reads a text claim without the JSON quoting SmallRye leaves on custom claims. */
    public static String textClaim(JsonWebToken jwt, String name) {
        if (jwt == null) {
            return null;
        }
        Object claim = jwt.getClaim(name);
        if (claim == null) {
            return null;
        }
        if (claim instanceof String text) {
            return text;
        }
        String json = String.valueOf(claim);
        return json.length() >= 2 && json.startsWith("\"") && json.endsWith("\"")
                ? json.substring(1, json.length() - 1)
                : json;
    }

    private JsonWebToken token() {
        try {
            JsonWebToken jwt = jwtInstance.get();
            return (jwt == null || jwt.getRawToken() == null) ? null : jwt;
        } catch (RuntimeException e) {
            // No active request context (scheduler, startup, JPA callback).
            return null;
        }
    }

    // ---------------------------------------------------------------- statics

    /** Safe accessor for JPA lifecycle callbacks that run outside CDI-managed code. */
    public static String usernameOrSystem() {
        try {
            return CDI.current().select(CurrentUser.class).get().username();
        } catch (RuntimeException e) {
            // Covers IllegalStateException, which CDI throws when no container exists
            // at all - for example during a JPA lifecycle callback at startup.
            return SYSTEM;
        }
    }

    public static Long companyIdOrNull() {
        try {
            return CDI.current().select(CurrentUser.class).get().companyId();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
