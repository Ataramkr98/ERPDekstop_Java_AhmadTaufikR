package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for FinExchangeRate.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/exchange-rates")
public class FinExchangeRateResource extends CrudResource<FinExchangeRate> {

    @Override
    protected Class<FinExchangeRate> entityType() {
        return FinExchangeRate.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "fromCurrency", "toCurrency" };
    }

    @Override
    protected String defaultSort() {
        return "rateDate";
    }

}
