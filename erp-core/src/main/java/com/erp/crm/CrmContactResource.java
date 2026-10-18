package com.erp.crm;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for CrmContact.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/crm/contacts")
public class CrmContactResource extends CrudResource<CrmContact> {

    @Override
    protected Class<CrmContact> entityType() {
        return CrmContact.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "firstName", "lastName", "email" };
    }

    @Override
    protected String defaultSort() {
        return "lastName";
    }

}
