package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrTimesheetLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/timesheet_lines")
public class HrTimesheetLineResource extends CrudResource<HrTimesheetLine> {

    @Override
    protected Class<HrTimesheetLine> entityType() {
        return HrTimesheetLine.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "notes" };
    }

    @Override
    protected String defaultSort() {
        return "workDate";
    }

}
