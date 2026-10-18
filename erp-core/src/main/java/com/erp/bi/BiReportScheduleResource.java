package com.erp.bi;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for BiReportSchedule.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/bi/report-schedules")
public class BiReportScheduleResource extends CrudResource<BiReportSchedule> {

    @Override
    protected Class<BiReportSchedule> entityType() {
        return BiReportSchedule.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "name" };
    }

    @Override
    protected String defaultSort() {
        return "nextRunAt";
    }

}
