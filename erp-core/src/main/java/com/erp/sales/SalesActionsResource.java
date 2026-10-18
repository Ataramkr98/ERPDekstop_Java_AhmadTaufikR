package com.erp.sales;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

/**
 * Sales business actions.
 *
 * <p>Deliberately a separate resource from the generated {@code SalOrderResource} and
 * friends: per docs/ARCHITECTURE.md section 3, adding a method-level JAX-RS annotation
 * to a subclass of {@code CrudResource} would suppress the inherited annotations and
 * silently remove the standard endpoints.
 */
@Authenticated
@Path("/api/sales/actions")
@Produces(MediaType.APPLICATION_JSON)
public class SalesActionsResource {

    @Inject
    SalesService sales;

    /** Ship confirm a delivery: decrements stock and posts the COGS journal. */
    @POST
    @Path("deliveries/{id}/ship")
    public SalDelivery ship(@PathParam("id") Long id) {
        return sales.ship(id);
    }

    /** Posts a customer invoice to AR and revenue. */
    @POST
    @Path("invoices/{id}/post")
    public SalInvoice postInvoice(@PathParam("id") Long id) {
        return sales.postInvoice(id);
    }
}
