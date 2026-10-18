package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrLeaveRequest.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/leave-requests")
public class HrLeaveRequestResource extends CrudResource<HrLeaveRequest> {

    @Override
    protected Class<HrLeaveRequest> entityType() {
        return HrLeaveRequest.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "reason" };
    }

    @Override
    protected String defaultSort() {
        return "startDate";
    }

}
