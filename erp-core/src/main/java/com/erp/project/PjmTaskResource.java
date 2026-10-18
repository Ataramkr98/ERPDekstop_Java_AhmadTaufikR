package com.erp.project;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PjmTask.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/project/tasks")
public class PjmTaskResource extends CrudResource<PjmTask> {

    @Override
    protected Class<PjmTask> entityType() {
        return PjmTask.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "wbsCode", "name" };
    }

    @Override
    protected String defaultSort() {
        return "wbsCode";
    }

}
