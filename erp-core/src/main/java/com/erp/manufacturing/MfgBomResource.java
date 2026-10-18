package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgBom.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/boms")
public class MfgBomResource extends CrudResource<MfgBom> {

    @Override
    protected Class<MfgBom> entityType() {
        return MfgBom.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "bomCode", "revision" };
    }

    @Override
    protected String defaultSort() {
        return "bomCode";
    }

}
