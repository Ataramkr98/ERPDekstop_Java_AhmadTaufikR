package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalOrderLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/order-lines")
public class SalOrderLineResource extends CrudResource<SalOrderLine> {

    @Override
    protected Class<SalOrderLine> entityType() {
        return SalOrderLine.class;
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
