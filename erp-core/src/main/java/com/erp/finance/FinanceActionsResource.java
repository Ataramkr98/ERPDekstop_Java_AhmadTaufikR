package com.erp.finance;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Authenticated @Path("/api/finance/actions") @Produces(MediaType.APPLICATION_JSON)
public class FinanceActionsResource {
    @Inject JournalService journals;
    @POST @Path("journals/{id}/reverse")
    public JournalEntry reverse(@PathParam("id") Long id) { return journals.reverse(id, "Requested from the desktop"); }
}
