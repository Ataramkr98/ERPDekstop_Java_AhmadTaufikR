package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgBomLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/bom_lines")
public class MfgBomLineResource extends CrudResource<MfgBomLine> {

    @Override
    protected Class<MfgBomLine> entityType() {
        return MfgBomLine.class;
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
