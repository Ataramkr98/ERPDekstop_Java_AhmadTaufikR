package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrAttendance.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/attendances")
public class HrAttendanceResource extends CrudResource<HrAttendance> {

    @Override
    protected Class<HrAttendance> entityType() {
        return HrAttendance.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "status" };
    }

    @Override
    protected String defaultSort() {
        return "attendanceDate";
    }

}
