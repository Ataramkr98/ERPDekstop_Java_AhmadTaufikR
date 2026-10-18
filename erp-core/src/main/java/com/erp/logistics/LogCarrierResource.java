package com.erp.logistics;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for LogCarrier.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/logistics/carriers")
public class LogCarrierResource extends CrudResource<LogCarrier> {

    @Override
    protected Class<LogCarrier> entityType() {
        return LogCarrier.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
