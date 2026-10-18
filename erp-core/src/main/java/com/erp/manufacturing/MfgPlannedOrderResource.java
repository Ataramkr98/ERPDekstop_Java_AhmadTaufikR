package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgPlannedOrder.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/planned-orders")
public class MfgPlannedOrderResource extends CrudResource<MfgPlannedOrder> {

    @Override
    protected Class<MfgPlannedOrder> entityType() {
        return MfgPlannedOrder.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "notes" };
    }

    @Override
    protected String defaultSort() {
        return "requiredDate";
    }

}
