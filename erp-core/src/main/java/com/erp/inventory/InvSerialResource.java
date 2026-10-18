package com.erp.inventory;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for InvSerial.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/inventory/serials")
public class InvSerialResource extends CrudResource<InvSerial> {

    @Override
    protected Class<InvSerial> entityType() {
        return InvSerial.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "serialNumber" };
    }

    @Override
    protected String defaultSort() {
        return "serialNumber";
    }

}
