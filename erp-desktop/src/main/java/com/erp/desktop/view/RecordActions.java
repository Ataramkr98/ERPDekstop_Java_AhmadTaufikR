package com.erp.desktop.view;

import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;

/** Desktop affordances for the existing transactional business endpoints. */
public final class RecordActions {
    private RecordActions() { }
    public record Action(String label, String route, String description, Set<String> statuses, boolean adminOnly) {
        public String path(String id) { return route.replace("{id}", id); }
        public boolean available(JsonNode row, List<String> roles) {
            if (row == null || (adminOnly && !roles.contains("ADMIN"))) return false;
            if (!statuses.contains(row.path("status").asText())) return false;
            if (route.endsWith("/reverse")) return !row.hasNonNull("reversedByEntryId")
                    && !Set.of("JOURNAL_REVERSAL", "GOODS_RECEIPT", "SALES_DELIVERY").contains(row.path("sourceDocumentType").asText());
            return !row.hasNonNull("journalEntryId");
        }
    }

    public static List<Action> forEntity(String entity) {
        return switch (entity) {
            case "SalInvoice" -> List.of(new Action("Post invoice", "/api/sales/actions/invoices/{id}/post",
                    "Post receivables and revenue, update the customer balance, and lock this invoice. Add invoice lines first.", Set.of("DRAFT"), false));
            case "SalDelivery" -> List.of(new Action("Ship delivery", "/api/sales/actions/deliveries/{id}/ship",
                    "Issue stock and post the cost-of-goods journal. Add delivery lines and check stock before shipping.", Set.of("PENDING", "PICKING", "PACKED"), false));
            case "PurGoodsReceipt" -> List.of(new Action("Post receipt", "/api/purchasing/actions/goods-receipts/{id}/post",
                    "Receive stock, post inventory accounting, and lock this receipt. Add receipt lines first.", Set.of("DRAFT"), false));
            case "PurInvoice" -> List.of(
                    new Action("Match invoice", "/api/purchasing/actions/invoices/{id}/match", "Compare invoice lines with purchase order and receipt lines.", Set.of("DRAFT", "MATCHED", "ON_HOLD"), false),
                    new Action("Release hold", "/api/purchasing/actions/invoices/{id}/release", "Approve posting outside matching tolerances. Review the variance before continuing.", Set.of("ON_HOLD"), true),
                    new Action("Post invoice", "/api/purchasing/actions/invoices/{id}/post", "Run matching and post payables. An out-of-tolerance invoice is placed on hold for review.", Set.of("DRAFT", "MATCHED", "APPROVED"), false));
            case "JournalEntry" -> List.of(new Action("Reverse journal", "/api/finance/actions/journals/{id}/reverse",
                    "Create a balancing reversal. Invoice reversals also void their source invoice. Stock journals and reversals cannot be reversed here.", Set.of("POSTED"), false));
            default -> List.of();
        };
    }

    /** Mirrors server document locks; the server remains authoritative. */
    public static boolean locked(String entity, JsonNode row) {
        if (row == null || !Set.of("SalInvoice", "SalDelivery", "PurInvoice", "PurGoodsReceipt").contains(entity)) return false;
        return row.hasNonNull("journalEntryId")
                || (entity.equals("PurInvoice") && row.path("status").asText().equals("APPROVED"))
                || Set.of("POSTED", "PAID", "PARTIALLY_PAID", "SHIPPED", "DELIVERED", "RECEIVED", "VOID").contains(row.path("status").asText());
    }
}
