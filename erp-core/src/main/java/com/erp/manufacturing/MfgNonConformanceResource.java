package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgNonConformance.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/non_conformances")
public class MfgNonConformanceResource extends CrudResource<MfgNonConformance> {

    @Override
    protected Class<MfgNonConformance> entityType() {
        return MfgNonConformance.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "ncrNumber", "defectCode" };
    }

    @Override
    protected String defaultSort() {
        return "raisedDate";
    }

}
