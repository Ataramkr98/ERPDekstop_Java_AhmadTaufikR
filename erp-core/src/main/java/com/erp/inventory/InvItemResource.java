package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvItem.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/items")
public class InvItemResource extends CrudResource<InvItem> {

    @Override
    protected Class<InvItem> entityType() {
        return InvItem.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "barcode", "category" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
