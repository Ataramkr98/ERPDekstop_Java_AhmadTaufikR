package com.erp.project;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PjmStatusReport.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/project/status-reports")
public class PjmStatusReportResource extends CrudResource<PjmStatusReport> {

    @Override
    protected Class<PjmStatusReport> entityType() {
        return PjmStatusReport.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "risksAndIssues" };
    }

    @Override
    protected String defaultSort() {
        return "reportDate";
    }

}
