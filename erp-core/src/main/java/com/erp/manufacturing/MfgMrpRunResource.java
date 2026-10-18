package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgMrpRun.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/mrp-runs")
public class MfgMrpRunResource extends CrudResource<MfgMrpRun> {

    @Override
    protected Class<MfgMrpRun> entityType() {
        return MfgMrpRun.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "status" };
    }

    @Override
    protected String defaultSort() {
        return "runDate";
    }

}
