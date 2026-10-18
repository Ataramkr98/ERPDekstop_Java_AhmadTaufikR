package com.erp.crm;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for CrmLead.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/crm/leads")
public class CrmLeadResource extends CrudResource<CrmLead> {

    @Override
    protected Class<CrmLead> entityType() {
        return CrmLead.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "leadNumber", "company", "contactName", "email" };
    }

    @Override
    protected String defaultSort() {
        return "leadNumber";
    }

}
