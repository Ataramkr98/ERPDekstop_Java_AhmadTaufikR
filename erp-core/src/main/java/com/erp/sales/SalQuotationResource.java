package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalQuotation.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/quotations")
public class SalQuotationResource extends CrudResource<SalQuotation> {

    @Override
    protected Class<SalQuotation> entityType() {
        return SalQuotation.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "quoteNumber", "notes" };
    }

    @Override
    protected String defaultSort() {
        return "quoteDate";
    }

}
