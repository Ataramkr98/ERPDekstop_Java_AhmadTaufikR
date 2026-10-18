package com.erp.crm;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for CrmOpportunity.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/crm/opportunities")
public class CrmOpportunityResource extends CrudResource<CrmOpportunity> {

    @Override
    protected Class<CrmOpportunity> entityType() {
        return CrmOpportunity.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "opportunityNumber", "name", "competitor" };
    }

    @Override
    protected String defaultSort() {
        return "expectedCloseDate";
    }

}
