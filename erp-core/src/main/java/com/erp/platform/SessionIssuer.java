package com.erp.platform;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Single place where a session token is minted.
 *
 * <p>Both the password login and the portfolio demo session go through here, so the
 * two can never drift apart: a demo session is an ordinary token with an ordinary
 * {@code companyId} tenancy claim, plus a {@code demo} flag that the client uses to
 * label the session and that the demo-only endpoints require. That flag carries no
 * extra authority - it only marks which tenant the session belongs to.
 */
@ApplicationScoped
public class SessionIssuer {

    static final int SESSION_HOURS = 12;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Inject
    AuditService audit;

    /** Mints a session token and returns the JSON body the desktop expects. */
    public Map<String, Object> issue(AppUser user, Company company, Long companyId, Set<String> roleCodes,
                                     boolean demo) {
        Set<String> groups = new HashSet<>(roleCodes);
        Instant expiresAt = Instant.now().plus(SESSION_HOURS, ChronoUnit.HOURS);

        var builder = Jwt.issuer(issuer)
                .subject(user.username)
                .groups(groups)
                .claim("companyId", companyId)
                .claim("fullName", user.fullName)
                .claim("demo", demo)
                .issuedAt(Instant.now().getEpochSecond())
                .expiresAt(expiresAt.getEpochSecond());
        String token = builder.sign();

        audit.custom(demo ? "DEMO_LOGIN" : "LOGIN", "AppUser", user.id,
                (demo ? "Demo session opened for " : "Successful login for ") + user.username);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("token", token);
        body.put("username", user.username);
        body.put("fullName", user.fullName);
        body.put("companyId", companyId);
        body.put("companyName", company == null ? null : company.name);
        body.put("roles", new ArrayList<>(roleCodes));
        body.put("locale", user.locale);
        body.put("demo", demo);
        body.put("mustChangePassword", Boolean.TRUE.equals(user.mustChangePassword));
        body.put("expiresAt", expiresAt.toString());
        return body;
    }

    /** Role codes granted to a user inside one company, defaulting to plain USER. */
    public List<String> roleCodes(Long userId, Long companyId) {
        List<String> roleCodes = new ArrayList<>(UserRole
                .<UserRole>find("userId = ?1 and companyId = ?2 and active = true", userId, companyId)
                .list().stream()
                .map(r -> r.roleCode)
                .distinct()
                .toList());
        if (roleCodes.isEmpty()) {
            roleCodes.add("USER");
        }
        return roleCodes;
    }
}