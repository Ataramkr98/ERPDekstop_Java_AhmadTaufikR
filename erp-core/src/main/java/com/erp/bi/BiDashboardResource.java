package com.erp.bi;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for BiDashboard.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/bi/dashboards")
public class BiDashboardResource extends CrudResource<BiDashboard> {

    @Override
    protected Class<BiDashboard> entityType() {
        return BiDashboard.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "roleCode" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
