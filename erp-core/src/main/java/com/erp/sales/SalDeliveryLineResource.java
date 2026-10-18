package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalDeliveryLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/delivery_lines")
public class SalDeliveryLineResource extends CrudResource<SalDeliveryLine> {

    @Override
    protected Class<SalDeliveryLine> entityType() {
        return SalDeliveryLine.class;
    }

    @Override
    protected String defaultSort() {
        return "lineNo";
    }

}
