package com.erp.project;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PjmResourcePlan.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/project/resource-plans")
public class PjmResourcePlanResource extends CrudResource<PjmResourcePlan> {

    @Override
    protected Class<PjmResourcePlan> entityType() {
        return PjmResourcePlan.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "role" };
    }

    @Override
    protected String defaultSort() {
        return "periodYear";
    }

}
