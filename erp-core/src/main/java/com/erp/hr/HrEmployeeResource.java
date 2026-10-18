package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrEmployee.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/employees")
public class HrEmployeeResource extends CrudResource<HrEmployee> {

    @Override
    protected Class<HrEmployee> entityType() {
        return HrEmployee.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "employeeNumber", "fullName", "firstName", "lastName", "email" };
    }

    @Override
    protected String defaultSort() {
        return "employeeNumber";
    }

}
