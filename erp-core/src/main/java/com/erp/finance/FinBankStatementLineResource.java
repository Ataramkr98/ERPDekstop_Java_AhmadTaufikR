package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for FinBankStatementLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/bank_statement_lines")
public class FinBankStatementLineResource extends CrudResource<FinBankStatementLine> {

    @Override
    protected Class<FinBankStatementLine> entityType() {
        return FinBankStatementLine.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "description", "reference", "counterparty" };
    }

    @Override
    protected String defaultSort() {
        return "lineNo";
    }

}
