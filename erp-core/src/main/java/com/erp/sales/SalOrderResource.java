package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalOrder.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/orders")
public class SalOrderResource extends CrudResource<SalOrder> {

    @Override
    protected Class<SalOrder> entityType() {
        return SalOrder.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "orderNumber", "customerReference", "notes" };
    }

    @Override
    protected String defaultSort() {
        return "orderDate";
    }

}
