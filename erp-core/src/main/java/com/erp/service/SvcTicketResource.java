package com.erp.service;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for SvcTicket.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/service/tickets")
public class SvcTicketResource extends CrudResource<SvcTicket> {

    @Override
    protected Class<SvcTicket> entityType() {
        return SvcTicket.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "ticketNumber", "subject", "description" };
    }

    @Override
    protected String defaultSort() {
        return "openedAt";
    }

}
