package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvCycleCountLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/cycle_count_lines")
public class InvCycleCountLineResource extends CrudResource<InvCycleCountLine> {

    @Override
    protected Class<InvCycleCountLine> entityType() {
        return InvCycleCountLine.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "notes" };
    }

    @Override
    protected String defaultSort() {
        return "lineNo";
    }

}
