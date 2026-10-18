package com.erp.platform;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for UserRole.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/platform/user_roles")
public class UserRoleResource extends CrudResource<UserRole> {

    @Override
    protected Class<UserRole> entityType() {
        return UserRole.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "roleCode" };
    }

    @Override
    protected String defaultSort() {
        return "roleCode";
    }

}
