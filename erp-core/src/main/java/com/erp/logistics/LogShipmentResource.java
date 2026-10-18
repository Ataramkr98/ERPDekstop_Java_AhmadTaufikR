package com.erp.logistics;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for LogShipment.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/logistics/shipments")
public class LogShipmentResource extends CrudResource<LogShipment> {

    @Override
    protected Class<LogShipment> entityType() {
        return LogShipment.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "shipmentNumber", "trackingNumber", "billOfLadingNumber" };
    }

    @Override
    protected String defaultSort() {
        return "shipDate";
    }

}
