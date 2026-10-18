package com.erp.purchasing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PurOrder.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/purchasing/orders")
public class PurOrderResource extends CrudResource<PurOrder> {

    @Override
    protected Class<PurOrder> entityType() {
        return PurOrder.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "orderNumber", "notes" };
    }

    @Override
    protected String defaultSort() {
        return "orderDate";
    }

}
