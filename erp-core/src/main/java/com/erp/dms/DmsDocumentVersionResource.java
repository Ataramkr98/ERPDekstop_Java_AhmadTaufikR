package com.erp.dms;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for DmsDocumentVersion.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/dms/document_versions")
public class DmsDocumentVersionResource extends CrudResource<DmsDocumentVersion> {

    @Override
    protected Class<DmsDocumentVersion> entityType() {
        return DmsDocumentVersion.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "fileName" };
    }

    @Override
    protected String defaultSort() {
        return "versionNumber";
    }

}
