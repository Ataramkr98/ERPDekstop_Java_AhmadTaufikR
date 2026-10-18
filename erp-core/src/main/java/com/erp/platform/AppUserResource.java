package com.erp.platform;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for AppUser.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/platform/app_users")
public class AppUserResource extends CrudResource<AppUser> {
    @jakarta.inject.Inject PasswordHasher hasher;

    @Override
    protected void beforeSave(AppUser user, boolean isNew) {
        if (user.password != null && !user.password.isBlank()) {
            if (user.password.length() < 8) throw com.erp.shared.ApiException.badRequest("Use a password with at least 8 characters.");
            user.passwordHash = hasher.hash(user.password);
        } else if (isNew) {
            throw com.erp.shared.ApiException.badRequest("A password with at least 8 characters is required for a new user.");
        } else {
            AppUser existing = AppUser.findById(user.id);
            user.passwordHash = existing.passwordHash;
        }
        if (isNew) {
            user.failedLoginCount = 0;
            user.mustChangePassword = true;
            user.status = user.status == null ? UserStatus.ACTIVE : user.status;
            user.defaultCompanyId = user.companyId;
        } else {
            AppUser existing = AppUser.findById(user.id);
            user.failedLoginCount = existing.failedLoginCount;
            user.lockedUntil = existing.lockedUntil;
            user.lastLoginAt = existing.lastLoginAt;
            user.mustChangePassword = existing.mustChangePassword;
            user.defaultCompanyId = existing.defaultCompanyId;
        }
    }

    @Override
    protected Class<AppUser> entityType() {
        return AppUser.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "username", "fullName", "email" };
    }

    @Override
    protected String defaultSort() {
        return "username";
    }

}
