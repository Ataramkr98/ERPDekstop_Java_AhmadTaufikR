package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalRMA.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/r_m_as")
public class SalRMAResource extends CrudResource<SalRMA> {

    @Override
    protected Class<SalRMA> entityType() {
        return SalRMA.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "rmaNumber", "reasonCode" };
    }

    @Override
    protected String defaultSort() {
        return "rmaDate";
    }

}
