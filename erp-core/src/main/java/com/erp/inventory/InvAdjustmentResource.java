package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvAdjustment.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/adjustments")
public class InvAdjustmentResource extends CrudResource<InvAdjustment> {

    @Override
    protected Class<InvAdjustment> entityType() {
        return InvAdjustment.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "adjustmentNumber", "reasonCode" };
    }

    @Override
    protected String defaultSort() {
        return "adjustmentDate";
    }

}
