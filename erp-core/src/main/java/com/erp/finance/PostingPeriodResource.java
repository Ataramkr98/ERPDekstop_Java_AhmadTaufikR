package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PostingPeriod.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/posting-periods")
public class PostingPeriodResource extends CrudResource<PostingPeriod> {

    @Override
    protected Class<PostingPeriod> entityType() {
        return PostingPeriod.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "name" };
    }

    @Override
    protected String defaultSort() {
        return "fiscalYear";
    }

}
