package com.erp.service;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SvcServiceContract.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/service/service_contracts")
public class SvcServiceContractResource extends CrudResource<SvcServiceContract> {

    @Override
    protected Class<SvcServiceContract> entityType() {
        return SvcServiceContract.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "contractNumber", "name" };
    }

    @Override
    protected String defaultSort() {
        return "contractNumber";
    }

}
