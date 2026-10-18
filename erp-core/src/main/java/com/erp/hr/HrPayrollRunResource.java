package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrPayrollRun.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/payroll-runs")
public class HrPayrollRunResource extends CrudResource<HrPayrollRun> {

    @Override
    protected Class<HrPayrollRun> entityType() {
        return HrPayrollRun.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "periodLabel" };
    }

    @Override
    protected String defaultSort() {
        return "periodYear";
    }

}
