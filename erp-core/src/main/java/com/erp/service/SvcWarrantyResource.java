package com.erp.service;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SvcWarranty.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/service/warranties")
public class SvcWarrantyResource extends CrudResource<SvcWarranty> {

    @Override
    protected Class<SvcWarranty> entityType() {
        return SvcWarranty.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "warrantyNumber" };
    }

    @Override
    protected String defaultSort() {
        return "startDate";
    }

}
