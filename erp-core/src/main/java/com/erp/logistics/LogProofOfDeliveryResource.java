package com.erp.logistics;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for LogProofOfDelivery.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/logistics/proof-of-delivery")
public class LogProofOfDeliveryResource extends CrudResource<LogProofOfDelivery> {

    @Override
    protected Class<LogProofOfDelivery> entityType() {
        return LogProofOfDelivery.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "receivedBy" };
    }

    @Override
    protected String defaultSort() {
        return "deliveredAt";
    }

}
