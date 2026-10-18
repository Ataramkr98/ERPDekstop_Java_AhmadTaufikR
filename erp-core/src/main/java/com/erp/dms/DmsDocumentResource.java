package com.erp.dms;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for DmsDocument.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/dms/documents")
public class DmsDocumentResource extends CrudResource<DmsDocument> {

    @Override
    protected Class<DmsDocument> entityType() {
        return DmsDocument.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "documentNumber", "name", "fileName", "searchText" };
    }

    @Override
    protected String defaultSort() {
        return "documentNumber";
    }

}
