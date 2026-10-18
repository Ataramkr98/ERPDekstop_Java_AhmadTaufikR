package com.erp.platform;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for Permission.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/platform/permissions")
public class PermissionResource extends CrudResource<Permission> {

    @Override
    protected Class<Permission> entityType() {
        return Permission.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "module" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
