package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for FinAccount.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/accounts")
public class FinAccountResource extends CrudResource<FinAccount> {

    @Override
    protected Class<FinAccount> entityType() {
        return FinAccount.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
