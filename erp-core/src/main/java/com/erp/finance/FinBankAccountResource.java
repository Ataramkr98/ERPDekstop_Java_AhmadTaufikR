package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for FinBankAccount.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/bank_accounts")
public class FinBankAccountResource extends CrudResource<FinBankAccount> {

    @Override
    protected Class<FinBankAccount> entityType() {
        return FinBankAccount.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "accountNumber" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
