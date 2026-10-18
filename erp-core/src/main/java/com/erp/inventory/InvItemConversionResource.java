package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvItemConversion.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/item-conversions")
public class InvItemConversionResource extends CrudResource<InvItemConversion> {

    @Override
    protected Class<InvItemConversion> entityType() {
        return InvItemConversion.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "uom" };
    }

    @Override
    protected String defaultSort() {
        return "uom";
    }

}
