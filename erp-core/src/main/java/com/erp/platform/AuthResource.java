package com.erp.platform;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.erp.config.CurrentUser;
import com.erp.shared.ApiException;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;

/**
 * Login and session introspection.
 *
 * <p>A successful login mints an RS256 JWT carrying the subject, the role codes as
 * groups and the active company as a private claim. {@link com.erp.config.CurrentUser}
 * reads that claim to scope every query, so tenancy is enforced from the token and
 * never from a value in the request body.
 */
@Path("/api/auth")
public class AuthResource {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Inject
    PasswordHasher hasher;

    @Inject
    AuditService audit;

    @Inject
    SessionIssuer sessions;

    @Inject
    JsonWebToken jwt;

    /** Credentials supplied by the login form. */
    public static class LoginRequest {
        public String username;
        public String password;
        public Long companyId;
    }

    @PermitAll
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(dontRollbackOn = ApiException.class)
    public Response login(LoginRequest request) {
        if (request == null || isBlank(request.username) || isBlank(request.password)) {
            throw ApiException.badRequest("Enter both a username and a password.");
        }
        String username = request.username.trim();

        // Demo identities share the configured display name with possible real
        // accounts, but can never be selected by normal sign-in.
        AppUser user = AppUser.find("username = ?1 and (companyId is null or companyId not in (select c.id from Company c where c.code = ?2))",
                username, DemoData.COMPANY_CODE).withLock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE).firstResult();
        if (user == null) {
            user = AppUser.find("username", username).withLock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE).firstResult();
        }
        if (user != null && !Boolean.TRUE.equals(user.active)) {
            throw ApiException.forbidden("This account is disabled. Contact your administrator.");
        }
        if (user != null && isLockedOut(user)) {
            throw lockedOut();
        }
        if (user == null || !hasher.verify(request.password, user.passwordHash)) {
            if (user != null) {
                recordFailure(user);
            }
            // Deliberately identical message for unknown user and wrong password so the
            // endpoint cannot be used to enumerate accounts.
            throw ApiException.badRequest("The username or password is incorrect.");
        }
        if (user.status == UserStatus.DISABLED) {
            throw ApiException.forbidden("This account is disabled. Ask an administrator to re-enable it.");
        }
        if (user.status == UserStatus.LOCKED) {
            throw lockedOut();
        }
        Company accountCompany = user.companyId == null ? null : Company.findById(user.companyId);
        if (accountCompany != null && DemoData.COMPANY_CODE.equals(accountCompany.code)) {
            throw ApiException.forbidden("Demo accounts use the demo entry point, not normal sign-in.");
        }

        user.failedLoginCount = 0;
        user.lockedUntil = null;
        user.lastLoginAt = Instant.now();

        if (request.companyId != null && !request.companyId.equals(user.defaultCompanyId)) {
            throw ApiException.forbidden("You cannot sign in to that company.");
        }
        Long companyId = user.defaultCompanyId;
        if (companyId == null) {
            Company first = user.companyId == null ? null : Company.findById(user.companyId);
            if (first == null) {
                throw ApiException.badRequest("No company has been set up yet. Contact your administrator.");
            }
            companyId = first.id;
        }
        user.defaultCompanyId = companyId;
        Company company = Company.findById(companyId);
        if (company == null || !Boolean.TRUE.equals(company.active) || Boolean.FALSE.equals(company.enabled)) {
            throw ApiException.forbidden("Your company is inactive. Contact your administrator.");
        }
        if (DemoData.COMPANY_CODE.equals(company.code)) {
            throw ApiException.forbidden("Demo accounts use the demo entry point, not normal sign-in.");
        }

        return Response.ok(sessions.issue(user, company, companyId,
                new HashSet<>(sessions.roleCodes(user.id, companyId)), false)).build();
    }

    @Authenticated
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> me() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", jwt.getName());
        // The raw claim is a JSON number; unwrap it or Jackson serialises the
        // JsonNumber wrapper instead of the value.
        body.put("companyId", asLong(jwt.getClaim("companyId")));
        // Custom claims come back wrapped in a JSON-P JsonValue, so a plain read would
        // serialise the wrapper rather than the value.
        body.put("fullName", CurrentUser.textClaim(jwt, "fullName"));
        body.put("roles", new ArrayList<>(jwt.getGroups()));
        body.put("demo", CurrentUser.booleanClaim(jwt, "demo"));
        return body;
    }

    private static Long asLong(Object claim) {
        if (claim instanceof Number number) {
            return number.longValue();
        }
        if (claim != null) {
            try {
                return Long.valueOf(claim.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @Authenticated
    @GET
    @Path("companies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Company> companies() {
        return Company.list("id = ?1 and active = true", asLong(jwt.getClaim("companyId")));
    }

    private void recordFailure(AppUser user) {
        int failures = (user.failedLoginCount == null ? 0 : user.failedLoginCount) + 1;
        user.failedLoginCount = failures;
        if (failures >= MAX_FAILED_ATTEMPTS) {
            user.lockedUntil = Instant.now().plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES);
            user.status = UserStatus.LOCKED;
            audit.custom("LOGIN_LOCKED", "AppUser", user.id,
                    "Account locked after " + failures + " failed attempts.");
        }
    }

    private static boolean isLockedOut(AppUser user) {
        return user.lockedUntil != null && user.lockedUntil.isAfter(Instant.now());
    }

    private static ApiException lockedOut() {
        return ApiException.forbidden("Too many failed attempts. Try again in " + LOCKOUT_MINUTES + " minutes.");
    }

    public static class PasswordRequest { public String currentPassword; public String newPassword; }

    @Authenticated @POST @Path("password") @Consumes(MediaType.APPLICATION_JSON) @Transactional
    public Response changePassword(PasswordRequest request) {
        if (CurrentUser.booleanClaim(jwt, "demo")) {
            throw ApiException.forbidden("Account administration is read-only in demo mode.");
        }
        AppUser user = AppUser.find("username = ?1 and defaultCompanyId = ?2", jwt.getName(), asLong(jwt.getClaim("companyId")))
                .withLock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE).firstResult();
        if (user == null || request == null || !hasher.verify(request.currentPassword, user.passwordHash))
            throw ApiException.badRequest("Your current password is incorrect.");
        if (request.newPassword == null || request.newPassword.length() < 8)
            throw ApiException.badRequest("Use a new password with at least 8 characters.");
        user.passwordHash = hasher.hash(request.newPassword);
        user.mustChangePassword = false;
        audit.custom("PASSWORD_CHANGED", "AppUser", user.id, "Account password changed.");
        return Response.noContent().build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
