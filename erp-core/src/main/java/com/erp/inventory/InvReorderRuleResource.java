package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvReorderRule.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/reorder-rules")
public class InvReorderRuleResource extends CrudResource<InvReorderRule> {

    @Override
    protected Class<InvReorderRule> entityType() {
        return InvReorderRule.class;
    }

    @Override
    protected String defaultSort() {
        return "itemId";
    }

}
