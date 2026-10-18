package com.erp.bi;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for BiKpi.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/bi/kpis")
public class BiKpiResource extends CrudResource<BiKpi> {

    @Override
    protected Class<BiKpi> entityType() {
        return BiKpi.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "category" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
