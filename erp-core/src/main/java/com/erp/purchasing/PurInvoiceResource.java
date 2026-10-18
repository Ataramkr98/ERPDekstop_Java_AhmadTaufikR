package com.erp.purchasing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PurInvoice.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/purchasing/invoices")
public class PurInvoiceResource extends CrudResource<PurInvoice> {

    @Override
    protected Class<PurInvoice> entityType() {
        return PurInvoice.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "invoiceNumber", "vendorInvoiceNumber" };
    }

    @Override
    protected String defaultSort() {
        return "invoiceDate";
    }

}
