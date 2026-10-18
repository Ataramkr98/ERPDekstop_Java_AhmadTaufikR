package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalDelivery.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/deliveries")
public class SalDeliveryResource extends CrudResource<SalDelivery> {

    @Override
    protected Class<SalDelivery> entityType() {
        return SalDelivery.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "deliveryNumber", "trackingNumber" };
    }

    @Override
    protected String defaultSort() {
        return "deliveryDate";
    }

}
