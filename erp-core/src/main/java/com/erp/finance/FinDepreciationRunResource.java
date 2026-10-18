package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for FinDepreciationRun.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/depreciation-runs")
public class FinDepreciationRunResource extends CrudResource<FinDepreciationRun> {

    @Override
    protected Class<FinDepreciationRun> entityType() {
        return FinDepreciationRun.class;
    }

    @Override
    protected String defaultSort() {
        return "periodYear";
    }

}
