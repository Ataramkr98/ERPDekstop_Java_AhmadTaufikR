package com.erp.crm;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for CrmActivity.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/crm/activities")
public class CrmActivityResource extends CrudResource<CrmActivity> {

    @Override
    protected Class<CrmActivity> entityType() {
        return CrmActivity.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "subject", "description" };
    }

    @Override
    protected String defaultSort() {
        return "dueDate";
    }

}
