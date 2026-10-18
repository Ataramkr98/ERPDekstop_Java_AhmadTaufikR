package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgWorkOrderOperation.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/work_order_operations")
public class MfgWorkOrderOperationResource extends CrudResource<MfgWorkOrderOperation> {

    @Override
    protected Class<MfgWorkOrderOperation> entityType() {
        return MfgWorkOrderOperation.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "description" };
    }

    @Override
    protected String defaultSort() {
        return "operationNo";
    }

}
