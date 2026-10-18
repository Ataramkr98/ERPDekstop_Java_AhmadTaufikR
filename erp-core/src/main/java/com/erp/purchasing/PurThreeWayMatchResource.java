package com.erp.purchasing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PurThreeWayMatch.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/purchasing/three-way-matches")
public class PurThreeWayMatchResource extends CrudResource<PurThreeWayMatch> {

    @Override
    protected Class<PurThreeWayMatch> entityType() {
        return PurThreeWayMatch.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "notes" };
    }

}
