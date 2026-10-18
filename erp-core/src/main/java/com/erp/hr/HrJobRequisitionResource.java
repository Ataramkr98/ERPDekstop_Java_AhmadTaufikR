package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrJobRequisition.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/job-requisitions")
public class HrJobRequisitionResource extends CrudResource<HrJobRequisition> {

    @Override
    protected Class<HrJobRequisition> entityType() {
        return HrJobRequisition.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "requisitionNumber", "title" };
    }

    @Override
    protected String defaultSort() {
        return "requisitionNumber";
    }

}
