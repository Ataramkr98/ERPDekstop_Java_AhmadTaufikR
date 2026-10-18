package com.erp.service;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SvcSurvey.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/service/surveys")
public class SvcSurveyResource extends CrudResource<SvcSurvey> {

    @Override
    protected Class<SvcSurvey> entityType() {
        return SvcSurvey.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "comment" };
    }

    @Override
    protected String defaultSort() {
        return "sentDate";
    }

}
