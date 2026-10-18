package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for JournalLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/journal-lines")
public class JournalLineResource extends CrudResource<JournalLine> {

    @Override
    protected Class<JournalLine> entityType() {
        return JournalLine.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "description" };
    }

    @Override
    protected String defaultSort() {
        return "lineNo";
    }

}
