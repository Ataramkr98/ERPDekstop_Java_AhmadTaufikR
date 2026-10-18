package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for FinBankStatement.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/bank_statements")
public class FinBankStatementResource extends CrudResource<FinBankStatement> {

    @Override
    protected Class<FinBankStatement> entityType() {
        return FinBankStatement.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "statementRef" };
    }

    @Override
    protected String defaultSort() {
        return "statementDate";
    }

}
