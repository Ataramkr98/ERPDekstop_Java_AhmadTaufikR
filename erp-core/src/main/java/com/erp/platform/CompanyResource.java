package com.erp.platform;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for Company.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/platform/companies")
public class CompanyResource extends CrudResource<Company> {

    @Override
    protected Class<Company> entityType() {
        return Company.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "taxId" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
