package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for JournalEntry.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/journal-entries")
public class JournalEntryResource extends CrudResource<JournalEntry> {

    @Override
    protected Class<JournalEntry> entityType() {
        return JournalEntry.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "entryNumber", "description", "reference" };
    }

    @Override
    protected String defaultSort() {
        return "postingDate";
    }

}
