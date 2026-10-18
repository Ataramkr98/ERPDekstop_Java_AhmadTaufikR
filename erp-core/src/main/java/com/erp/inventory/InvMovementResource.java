package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvMovement.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/movements")
public class InvMovementResource extends CrudResource<InvMovement> {

    @Override
    protected Class<InvMovement> entityType() {
        return InvMovement.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "movementNumber", "reasonCode", "notes" };
    }

    @Override
    protected String defaultSort() {
        return "movementDate";
    }

}
