package com.erp.logistics;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for LogRouteStop.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/logistics/route_stops")
public class LogRouteStopResource extends CrudResource<LogRouteStop> {

    @Override
    protected Class<LogRouteStop> entityType() {
        return LogRouteStop.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "city" };
    }

    @Override
    protected String defaultSort() {
        return "stopNo";
    }

}
