package com.erp.purchasing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PurVendor.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/purchasing/vendors")
public class PurVendorResource extends CrudResource<PurVendor> {

    @Override
    protected Class<PurVendor> entityType() {
        return PurVendor.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "email", "taxId" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
