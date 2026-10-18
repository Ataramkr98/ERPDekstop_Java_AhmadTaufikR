package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrTimesheet.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/timesheets")
public class HrTimesheetResource extends CrudResource<HrTimesheet> {

    @Override
    protected Class<HrTimesheet> entityType() {
        return HrTimesheet.class;
    }

    @Override
    protected String defaultSort() {
        return "weekStartDate";
    }

}
