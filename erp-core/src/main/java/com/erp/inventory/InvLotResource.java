package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvLot.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/lots")
public class InvLotResource extends CrudResource<InvLot> {

    @Override
    protected Class<InvLot> entityType() {
        return InvLot.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "lotNumber", "supplierLotNumber" };
    }

    @Override
    protected String defaultSort() {
        return "lotNumber";
    }

}
