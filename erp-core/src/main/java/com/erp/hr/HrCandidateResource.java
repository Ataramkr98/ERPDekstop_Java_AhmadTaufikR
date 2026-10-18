package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrCandidate.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/candidates")
public class HrCandidateResource extends CrudResource<HrCandidate> {

    @Override
    protected Class<HrCandidate> entityType() {
        return HrCandidate.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "firstName", "lastName", "email" };
    }

    @Override
    protected String defaultSort() {
        return "appliedAt";
    }

}
