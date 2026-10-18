package com.erp.crm;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for CrmAccount.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/crm/accounts")
public class CrmAccountResource extends CrudResource<CrmAccount> {

    @Override
    protected Class<CrmAccount> entityType() {
        return CrmAccount.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "industry" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
