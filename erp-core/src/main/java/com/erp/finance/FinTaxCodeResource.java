package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for FinTaxCode.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/tax_codes")
public class FinTaxCodeResource extends CrudResource<FinTaxCode> {

    @Override
    protected Class<FinTaxCode> entityType() {
        return FinTaxCode.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "jurisdiction" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
