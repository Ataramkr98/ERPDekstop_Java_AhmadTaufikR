package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvCycleCount.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/cycle-counts")
public class InvCycleCountResource extends CrudResource<InvCycleCount> {

    @Override
    protected Class<InvCycleCount> entityType() {
        return InvCycleCount.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "countNumber" };
    }

    @Override
    protected String defaultSort() {
        return "countDate";
    }

}
