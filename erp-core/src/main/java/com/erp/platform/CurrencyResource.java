package com.erp.platform;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for Currency.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/platform/currencies")
public class CurrencyResource extends CrudResource<Currency> {

    @Override
    protected Class<Currency> entityType() {
        return Currency.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

    /** Reference data shared across companies. */
    @Override
    protected boolean companyScoped() {
        return false;
    }

}
