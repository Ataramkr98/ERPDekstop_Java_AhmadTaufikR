package com.erp.sales;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SalCustomer.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/sales/customers")
public class SalCustomerResource extends CrudResource<SalCustomer> {

    @Override
    protected Class<SalCustomer> entityType() {
        return SalCustomer.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "name", "email", "taxId" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
