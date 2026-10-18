package com.erp.bi;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for BiKpiValue.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/bi/kpi-values")
public class BiKpiValueResource extends CrudResource<BiKpiValue> {

    @Override
    protected Class<BiKpiValue> entityType() {
        return BiKpiValue.class;
    }

    @Override
    protected String defaultSort() {
        return "valueDate";
    }

}
