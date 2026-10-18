package com.erp.platform;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for AuditLog.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/platform/audit_logs")
public class AuditLogResource extends CrudResource<AuditLog> {

    @Override
    protected Class<AuditLog> entityType() {
        return AuditLog.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "username", "entityType", "action" };
    }

    @Override
    protected String defaultSort() {
        return "occurredAt";
    }

}
