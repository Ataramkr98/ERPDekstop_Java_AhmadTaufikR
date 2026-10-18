package com.erp.purchasing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PurRequisitionLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/purchasing/requisition_lines")
public class PurRequisitionLineResource extends CrudResource<PurRequisitionLine> {

    @Override
    protected Class<PurRequisitionLine> entityType() {
        return PurRequisitionLine.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "description" };
    }

    @Override
    protected String defaultSort() {
        return "lineNo";
    }

}
