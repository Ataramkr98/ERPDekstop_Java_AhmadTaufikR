package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrContract.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/contracts")
public class HrContractResource extends CrudResource<HrContract> {

    @Override
    protected Class<HrContract> entityType() {
        return HrContract.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "contractNumber" };
    }

    @Override
    protected String defaultSort() {
        return "startDate";
    }

}
