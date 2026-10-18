package com.erp.project;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PjmExpense.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/project/expenses")
public class PjmExpenseResource extends CrudResource<PjmExpense> {

    @Override
    protected Class<PjmExpense> entityType() {
        return PjmExpense.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "description", "category" };
    }

    @Override
    protected String defaultSort() {
        return "expenseDate";
    }

}
