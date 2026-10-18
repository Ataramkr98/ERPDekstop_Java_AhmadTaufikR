package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalInvoice.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/invoices")
public class SalInvoiceResource extends CrudResource<SalInvoice> {

    @Override
    protected Class<SalInvoice> entityType() {
        return SalInvoice.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "invoiceNumber", "notes" };
    }

    @Override
    protected String defaultSort() {
        return "invoiceDate";
    }

}
