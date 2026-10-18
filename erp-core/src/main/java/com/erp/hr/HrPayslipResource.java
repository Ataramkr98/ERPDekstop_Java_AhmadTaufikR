package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrPayslip.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/payslips")
public class HrPayslipResource extends CrudResource<HrPayslip> {

    @Override
    protected Class<HrPayslip> entityType() {
        return HrPayslip.class;
    }

    @Override
    protected String defaultSort() {
        return "periodYear";
    }

}
