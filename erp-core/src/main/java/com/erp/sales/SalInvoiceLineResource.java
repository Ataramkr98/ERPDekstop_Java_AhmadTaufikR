package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalInvoiceLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/invoice_lines")
public class SalInvoiceLineResource extends CrudResource<SalInvoiceLine> {

    @Override
    protected Class<SalInvoiceLine> entityType() {
        return SalInvoiceLine.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "description" };
    }

    @Override
    protected String defaultSort() {
        return "lineNo";
    }

}
