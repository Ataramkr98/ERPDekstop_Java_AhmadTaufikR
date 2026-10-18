package com.erp.platform;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.erp.config.CurrentUser;
import com.erp.shared.ApiException;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Portfolio demo workspace.
 *
 * <p>Three properties hold, and each is enforced rather than assumed:
 * <ol>
 *   <li><b>No auth bypass on a real deployment.</b> Every endpoint here is disabled
 *       unless {@code erp.demo.enabled=true}, which defaults to {@code false} and is
 *       forced off again whenever the Quarkus profile is a production profile. While
 *       disabled, {@link #status()} and {@link #session()} answer {@code 404}, so a
 *       real install has no credential-free sign-in path at all.</li>
 *   <li><b>Tenant isolation.</b> The demo session is an ordinary JWT whose
 *       {@code companyId} claim points at the demo tenant, so every query in the app
 *       is scoped away from real companies; {@link #reset()} deletes only rows carrying
 *       the demo {@code companyId}.</li>
 *   <li><b>No outbound side effects.</b> This application has no payment, messaging or
 *       webhook integration, so the demo path cannot reach a real system.</li>
 * </ol>
 */
@Path("/api/demo")
@Produces(MediaType.APPLICATION_JSON)
public class DemoResource {

    /**
     * Budget for the two endpoints that (re)build the demo dataset.
     *
     * <p>Seeding touches nearly every entity in the platform, so the first
     * {@code POST /api/demo/session} after a cold start has to load a large slice of the
     * application while the transaction is open. The JTA default of 60 seconds is a
     * request-shaped assumption that does not fit that one-off cost: on a cold or
     * slow-disk machine the transaction reaper aborts the seeding mid-flight, the commit
     * then fails with {@code ARJUNA016102}, and the reviewer sees a 500 on the demo
     * button. {@code jakarta.transaction.Transactional} has no per-method timeout, so
     * these two endpoints open their transaction explicitly through
     * {@link QuarkusTransaction} and give it this budget. It stays bounded so a wedged
     * seed cannot hold the transaction open indefinitely.
     */
    private static final int SEED_TIMEOUT_SECONDS = 300;

    @Inject
    DemoSettings settings;

    @Inject
    DemoData demoData;

    @Inject
    PasswordHasher hasher;

    @Inject
    SessionIssuer sessions;

    @Inject
    CurrentUser currentUser;

    /** Lets the sign-in screen decide whether to offer the demo button at all. */
    @GET
    @Path("status")
    @PermitAll
    public Response status() {
        if (!settings.enabled()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("enabled", false)).build();
        }
        return Response.ok(Map.of(
                "enabled", true,
                "username", settings.username(),
                "companyCode", DemoData.COMPANY_CODE,
                "blurb", "Synthetic dataset in an isolated demo tenant. Nothing leaves this machine."))
                .build();
    }

    /** Issues a demo session without a password. Disabled builds get a 404. */
    @POST
    @Path("session")
    @PermitAll
    public Response session() {
        if (!settings.enabled()) {
            throw new NotFoundException("Demo mode is not enabled.");
        }
        Map<String, Object> body = inSeedTransaction(() -> {
            demoData.ensureWorkspace();
            Company company = demoData.company();
            AppUser user = demoUser(company);
            return sessions.issue(user, company, company.id,
                    new HashSet<>(sessions.roleCodes(user.id, company.id)), true);
        });
        return Response.ok(body).build();
    }

    /** Returns the demo tenant to its documented starting point. */
    @POST
    @Path("reset")
    @Authenticated
    public Map<String, Object> reset() {
        requireDemoSession();
        Long demoCompanyId = demoData.company().id;
        return inSeedTransaction(() -> {
            demoData.resetTenant(demoCompanyId);
            return demoData.ensureWorkspace();
        });
    }

    /**
     * Ends the demo session. Tokens are stateless, so the real work is done on the
     * client: it drops the token and returns to the sign-in screen. This endpoint
     * exists so the client has something to acknowledge, and so the flow is visible in
     * the audit log rather than being a silent local-only reset.
     */
    @DELETE
    @Path("session")
    @Authenticated
    @Transactional
    public Response exit() {
        requireDemoSession();
        return Response.noContent().build();
    }

    // ------------------------------------------------------------------ internals

    private void requireDemoSession() {
        if (!settings.enabled()) {
            throw new NotFoundException("Demo mode is not enabled.");
        }
        if (!currentUser.isDemo()) {
            throw ApiException.forbidden("This is not a demo session.");
        }
    }

    /**
     * Runs a dataset (re)build in a transaction of its own carrying the seed budget.
     *
     * <p>A transaction is required rather than joined because a JTA timeout only takes
     * effect on the transaction that starts it: joining an ambient one would silently
     * keep the 60-second default. The {@code @Transactional} services called inside
     * ({@link DemoData#ensureWorkspace()} and {@link DemoData#resetTenant(Long)}) then
     * join this transaction, so the whole rebuild commits or rolls back as one unit.
     */
    private static <T> T inSeedTransaction(Callable<T> work) {
        return QuarkusTransaction.requiringNew().timeout(SEED_TIMEOUT_SECONDS).call(work);
    }

    /**
     * The demo login. Its password is a long random value that is never displayed,
     * stored in configuration, or returned by any endpoint - the demo is entered via
     * the button, never by typing a credential, so nobody can sign in as it by hand.
     */
    private AppUser demoUser(Company company) {
        String username = settings.username();
        AppUser existing = AppUser.<AppUser>find("username = ?1 and companyId = ?2", username, company.id)
                .firstResult();
        if (existing != null) {
            return existing;
        }
        AppUser user = new AppUser();
        user.username = username;
        user.fullName = "Demo Reviewer";
        user.email = "demo@example.invalid";
        user.passwordHash = hasher.hash(randomPassword());
        user.locale = "en-US";
        user.defaultCompanyId = company.id;
        user.status = UserStatus.ACTIVE;
        user.failedLoginCount = 0;
        user.mustChangePassword = Boolean.FALSE;
        user.companyId = company.id;
        user.persist();

        UserRole role = new UserRole();
        role.userId = user.id;
        role.roleCode = "ADMIN";
        role.companyId = company.id;
        role.persist();
        return user;
    }

    private static String randomPassword() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}