package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalPriceListLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/price-list-lines")
public class SalPriceListLineResource extends CrudResource<SalPriceListLine> {

    @Override
    protected Class<SalPriceListLine> entityType() {
        return SalPriceListLine.class;
    }

    @Override
    protected String defaultSort() {
        return "minQuantity";
    }

}
