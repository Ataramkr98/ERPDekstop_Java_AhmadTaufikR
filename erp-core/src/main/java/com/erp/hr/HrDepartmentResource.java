package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrDepartment.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/departments")
public class HrDepartmentResource extends CrudResource<HrDepartment> {

    @Override
    protected Class<HrDepartment> entityType() {
        return HrDepartment.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
