package com.erp.project;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PjmMilestone.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/project/milestones")
public class PjmMilestoneResource extends CrudResource<PjmMilestone> {

    @Override
    protected Class<PjmMilestone> entityType() {
        return PjmMilestone.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "name" };
    }

    @Override
    protected String defaultSort() {
        return "plannedDate";
    }

}
