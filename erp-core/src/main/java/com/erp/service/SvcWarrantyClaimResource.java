package com.erp.service;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SvcWarrantyClaim.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/service/warranty-claims")
public class SvcWarrantyClaimResource extends CrudResource<SvcWarrantyClaim> {

    @Override
    protected Class<SvcWarrantyClaim> entityType() {
        return SvcWarrantyClaim.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "claimNumber", "defectCode" };
    }

    @Override
    protected String defaultSort() {
        return "claimDate";
    }

}
