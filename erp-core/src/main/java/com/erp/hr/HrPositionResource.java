package com.erp.hr;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for HrPosition.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/hr/positions")
public class HrPositionResource extends CrudResource<HrPosition> {

    @Override
    protected Class<HrPosition> entityType() {
        return HrPosition.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "code", "title" };
    }

    @Override
    protected String defaultSort() {
        return "code";
    }

}
