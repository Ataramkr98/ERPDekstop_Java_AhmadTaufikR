package com.erp.project;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PjmProject.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/project/projects")
public class PjmProjectResource extends CrudResource<PjmProject> {

    @Override
    protected Class<PjmProject> entityType() {
        return PjmProject.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
