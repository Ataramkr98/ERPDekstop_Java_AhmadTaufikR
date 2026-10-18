package com.erp.purchasing;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

/** Procurement business actions: posting, matching and hold release. */
@Authenticated
@Path("/api/purchasing/actions")
@Produces(MediaType.APPLICATION_JSON)
public class PurchasingActionsResource {

    @Inject
    PurchasingService purchasing;
    @Inject com.erp.shared.BusinessRecords records;
    @Inject com.erp.platform.AuditService audit;

    /** Posts a goods receipt: increases stock and credits the GRNI clearing account. */
    @POST
    @Path("goods-receipts/{id}/post")
    public PurGoodsReceipt postGoodsReceipt(@PathParam("id") Long id) {
        return purchasing.postGoodsReceipt(id);
    }

    /** Runs the three-way match and posts the vendor invoice to AP. */
    @POST
    @Path("invoices/{id}/post")
    public PurInvoice postInvoice(@PathParam("id") Long id) {
        return purchasing.postVendorInvoice(id);
    }

    /** Re-runs the match without posting, so an AP clerk can see the variances. */
    @POST
    @Path("invoices/{id}/match")
    @jakarta.transaction.Transactional
    public Map<String, Object> match(@PathParam("id") Long id) {
        PurInvoice invoice = records.lock(PurInvoice.class, id);
        com.erp.shared.RecordPolicy.editable(invoice);
        if (invoice == null) {
            throw com.erp.shared.ApiException.notFound("Vendor invoice " + id + " was not found.");
        }
        MatchStatus status = purchasing.runThreeWayMatch(invoice);
        invoice.matchStatus = status;
        audit.custom("MATCH", "PurInvoice", id, "Three-way match: " + status.name());
        return Map.of("invoiceId", id, "matchStatus", status.name());
    }

    /** Releases a tolerance hold so the invoice can be posted deliberately. */
    @POST
    @Path("invoices/{id}/release")
    @jakarta.annotation.security.RolesAllowed("ADMIN")
    public PurInvoice release(@PathParam("id") Long id) {
        return purchasing.releaseHold(id);
    }
}
