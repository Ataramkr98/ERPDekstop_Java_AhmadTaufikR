package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgWorkOrder.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/work-orders")
public class MfgWorkOrderResource extends CrudResource<MfgWorkOrder> {

    @Override
    protected Class<MfgWorkOrder> entityType() {
        return MfgWorkOrder.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "workOrderNumber", "notes" };
    }

    @Override
    protected String defaultSort() {
        return "plannedStartDate";
    }

}
