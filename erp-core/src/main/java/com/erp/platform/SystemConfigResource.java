package com.erp.platform;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SystemConfig.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/platform/system_configs")
public class SystemConfigResource extends CrudResource<SystemConfig> {

    @Override
    protected Class<SystemConfig> entityType() {
        return SystemConfig.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "configKey", "scope" };
    }

    @Override
    protected String defaultSort() {
        return "configKey";
    }

}
