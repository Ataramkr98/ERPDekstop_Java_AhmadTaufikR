package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvStock.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/stocks")
public class InvStockResource extends CrudResource<InvStock> {

    @Override
    protected Class<InvStock> entityType() {
        return InvStock.class;
    }

    @Override
    protected String defaultSort() {
        return "itemId";
    }

}
