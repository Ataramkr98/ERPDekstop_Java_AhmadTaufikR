package com.erp.service;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SvcKnowledgeArticle.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/service/kb-articles")
public class SvcKnowledgeArticleResource extends CrudResource<SvcKnowledgeArticle> {

    @Override
    protected Class<SvcKnowledgeArticle> entityType() {
        return SvcKnowledgeArticle.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "articleNumber", "title", "body" };
    }

    @Override
    protected String defaultSort() {
        return "articleNumber";
    }

}
