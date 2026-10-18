package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgMaterialIssue.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/material_issues")
public class MfgMaterialIssueResource extends CrudResource<MfgMaterialIssue> {

    @Override
    protected Class<MfgMaterialIssue> entityType() {
        return MfgMaterialIssue.class;
    }

    @Override
    protected String defaultSort() {
        return "issueDate";
    }

}
