package com.erp.bi;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for BiTile.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/bi/tiles")
public class BiTileResource extends CrudResource<BiTile> {

    @Override
    protected Class<BiTile> entityType() {
        return BiTile.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "title" };
    }

    @Override
    protected String defaultSort() {
        return "rowIndex";
    }

}
