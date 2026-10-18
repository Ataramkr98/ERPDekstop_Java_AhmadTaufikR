package com.erp.logistics;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for LogRoute.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/logistics/routes")
public class LogRouteResource extends CrudResource<LogRoute> {

    @Override
    protected Class<LogRoute> entityType() {
        return LogRoute.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "routeNumber", "vehiclePlate" };
    }

    @Override
    protected String defaultSort() {
        return "plannedDeparture";
    }

}
