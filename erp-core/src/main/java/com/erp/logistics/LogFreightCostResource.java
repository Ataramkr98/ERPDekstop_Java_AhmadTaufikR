package com.erp.logistics;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for LogFreightCost.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/logistics/freight-costs")
public class LogFreightCostResource extends CrudResource<LogFreightCost> {

    @Override
    protected Class<LogFreightCost> entityType() {
        return LogFreightCost.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "costType" };
    }

    @Override
    protected String defaultSort() {
        return "accrualDate";
    }

}
