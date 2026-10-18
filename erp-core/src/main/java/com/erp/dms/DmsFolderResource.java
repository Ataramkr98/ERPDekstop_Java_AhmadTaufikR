package com.erp.dms;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for DmsFolder.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/dms/folders")
public class DmsFolderResource extends CrudResource<DmsFolder> {

    @Override
    protected Class<DmsFolder> entityType() {
        return DmsFolder.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "name" };
    }

    @Override
    protected String defaultSort() {
        return "name";
    }

}
