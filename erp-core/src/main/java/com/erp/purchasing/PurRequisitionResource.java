package com.erp.purchasing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PurRequisition.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/purchasing/requisitions")
public class PurRequisitionResource extends CrudResource<PurRequisition> {

    @Override
    protected Class<PurRequisition> entityType() {
        return PurRequisition.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "requisitionNumber", "justification" };
    }

    @Override
    protected String defaultSort() {
        return "requestDate";
    }

}
