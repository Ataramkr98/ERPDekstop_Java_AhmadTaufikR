package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalCommission.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/commissions")
public class SalCommissionResource extends CrudResource<SalCommission> {

    @Override
    protected Class<SalCommission> entityType() {
        return SalCommission.class;
    }

    @Override
    protected String defaultSort() {
        return "periodYear";
    }

}
