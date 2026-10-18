package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgRouting.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/routings")
public class MfgRoutingResource extends CrudResource<MfgRouting> {

    @Override
    protected Class<MfgRouting> entityType() {
        return MfgRouting.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "routingCode" };
    }

    @Override
    protected String defaultSort() {
        return "routingCode";
    }

}
