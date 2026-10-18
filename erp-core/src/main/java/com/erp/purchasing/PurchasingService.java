package com.erp.purchasing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.erp.finance.JournalService;
import com.erp.inventory.InventoryService;
import com.erp.inventory.MovementType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.erp.shared.ApiException;

/**
 * Procure-to-pay posting (M-PUR-004, M-PUR-005, M-PUR-008).
 *
 * <p>Goods receipt debits Inventory and credits the GRNI clearing account, so stock is
 * on the books the moment it arrives. The vendor invoice then debits GRNI (or expense
 * for a service) and credits Accounts Payable, which is what makes the clearing account
 * reconcile to zero when everything has been invoiced.
 */
@ApplicationScoped
public class PurchasingService {

    /** Default price and quantity tolerance for the three-way match (M-PUR-005). */
    private static final BigDecimal DEFAULT_QTY_TOLERANCE = new BigDecimal("5");
    private static final BigDecimal DEFAULT_PRICE_TOLERANCE = new BigDecimal("2");

    @Inject
    EntityManager em;
    @Inject com.erp.shared.BusinessRecords records;
    @Inject com.erp.platform.AuditService audit;

    @Inject
    JournalService journals;

    @Inject
    InventoryService inventory;

    @Transactional
    public PurGoodsReceipt postGoodsReceipt(Long receiptId) {
        PurGoodsReceipt receipt = records.lock(PurGoodsReceipt.class, receiptId);
        records.lock(PurVendor.class, receipt.vendorId);
        if (receipt.status != GoodsReceiptStatus.DRAFT) throw ApiException.conflict("Only a draft goods receipt can be posted.");
        if (receipt == null) {
            throw ApiException.notFound("Goods receipt " + receiptId + " was not found.");
        }
        if (receipt.status == GoodsReceiptStatus.POSTED) {
            throw ApiException.conflict("Goods receipt " + receipt.receiptNumber + " has already been posted.");
        }

        List<PurGoodsReceiptLine> lines = em.createQuery(
                "select l from PurGoodsReceiptLine l where l.active = true and l.receiptId = :r order by l.lineNo",
                PurGoodsReceiptLine.class)
                .setParameter("r", receiptId)
                .getResultList();
        if (lines.isEmpty()) {
            throw ApiException.badRequest("Goods receipt " + receipt.receiptNumber
                    + " has no lines. Add at least one line before posting.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (PurGoodsReceiptLine line : lines) {
            com.erp.shared.RecordPolicy.owned(line, receipt.companyId);
            if (nz(line.quantity).signum() <= 0) throw ApiException.badRequest("Receipt quantities must be positive.");
            InventoryService.MovementRequest move = new InventoryService.MovementRequest(
                    MovementType.GOODS_RECEIPT, line.itemId,
                    // Lines may omit the warehouse (the UI and the demo seed both set it
                    // on the receipt header); fall back to the receipt's warehouse so
                    // posting a header-level receipt is not rejected as a movement
                    // without a warehouse.
                    line.warehouseId != null ? line.warehouseId : receipt.warehouseId,
                    nz(line.quantity));
            move.locationId = line.locationId;
            move.lotId = line.lotId;
            move.uom = line.uom;
            move.unitCost = line.unitCost;
            move.sourceDocumentType = "GOODS_RECEIPT";
            move.sourceDocumentId = receiptId;
            move.movementDate = receipt.receiptDate;
            move.notes = "Receipt " + receipt.receiptNumber;

            total = total.add(inventory.record(move).totalCost);

            if (line.orderLineId != null) {
                PurOrderLine orderLine = records.lock(PurOrderLine.class, line.orderLineId);
                if (orderLine != null) {
                    orderLine.receivedQuantity = nz(orderLine.receivedQuantity).add(nz(line.quantity));
                    em.merge(orderLine);
                }
            }
        }

        List<JournalService.Line> journalLines = new ArrayList<>();
        journalLines.add(new JournalService.Line(
                journals.accountIdByCode("1300"), total, BigDecimal.ZERO, "Inventory - " + receipt.receiptNumber));
        journalLines.add(new JournalService.Line(
                journals.accountIdByCode("1400"), BigDecimal.ZERO, total, "GRNI - " + receipt.receiptNumber));

        var entry = journals.post("GOODS_RECEIPT", receiptId, receipt.receiptDate,
                "Goods receipt " + receipt.receiptNumber, journalLines);

        receipt.status = GoodsReceiptStatus.POSTED;
        receipt.totalValue = total;
        receipt.journalEntryId = entry.id;
        audit.custom("POST", "PurGoodsReceipt", receipt.id, "Posted goods receipt " + receipt.receiptNumber);
        em.merge(receipt);

        if (receipt.orderId != null) {
            recalculateOrderStatus(receipt.orderId);
        }
        return receipt;
    }

    /**
     * Posts a vendor invoice after running the three-way match. An out-of-tolerance
     * match leaves the invoice on hold rather than posting a wrong AP balance.
     */
    @Transactional
    public PurInvoice postVendorInvoice(Long invoiceId) {
        PurInvoice invoice = records.lock(PurInvoice.class, invoiceId);
        records.lock(PurVendor.class, invoice.vendorId);
        if (invoice.journalEntryId != null || invoice.status == VendorInvoiceStatus.VOID) throw ApiException.conflict("This invoice is posted or void.");
        if (invoice == null) {
            throw ApiException.notFound("Vendor invoice " + invoiceId + " was not found.");
        }
        if (invoice.status == VendorInvoiceStatus.POSTED || invoice.status == VendorInvoiceStatus.PAID) {
            throw ApiException.conflict("Vendor invoice " + invoice.invoiceNumber + " has already been posted.");
        }
        if (invoice.status == VendorInvoiceStatus.ON_HOLD) {
            throw ApiException.conflict("Vendor invoice " + invoice.invoiceNumber
                    + " is on hold. " + (invoice.holdReason == null ? "" : invoice.holdReason));
        }

        BigDecimal total = nz(invoice.totalAmount);
        if (total.signum() <= 0) {
            throw ApiException.badRequest("Vendor invoice " + invoice.invoiceNumber
                    + " has no value. Add lines before posting.");
        }

        boolean released = invoice.status == VendorInvoiceStatus.APPROVED;
        MatchStatus match = runThreeWayMatch(invoice);
        invoice.matchStatus = match;
        if (!released && (match == MatchStatus.QUANTITY_VARIANCE || match == MatchStatus.PRICE_VARIANCE
                || match == MatchStatus.BOTH_VARIANCE)) {
            invoice.status = VendorInvoiceStatus.ON_HOLD;
            invoice.holdReason = "Three-way match is outside tolerance (" + match.name()
                    + "). Review the match lines, then release the hold to post.";
            em.merge(invoice);
            return invoice;
        }

        List<PurInvoiceLine> lines = em.createQuery(
                "select l from PurInvoiceLine l where l.active = true and l.invoiceId = :i order by l.lineNo",
                PurInvoiceLine.class)
                .setParameter("i", invoiceId)
                .getResultList();

        // A receipt-backed invoice settles the GRNI clearing account; a service or
        // non-stock invoice goes straight to the expense account on the line.
        BigDecimal toClearing = BigDecimal.ZERO;
        BigDecimal toExpense = BigDecimal.ZERO;
        for (PurInvoiceLine line : lines) {
            if (line.receiptLineId != null) {
                toClearing = toClearing.add(nz(line.lineTotal));
            } else {
                toExpense = toExpense.add(nz(line.lineTotal));
            }
        }
        if (lines.isEmpty()) throw ApiException.badRequest("Add invoice lines before posting.");
        BigDecimal inputTax = lines.stream().map(l -> nz(l.taxAmount)).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (inputTax.signum() < 0 || toClearing.add(toExpense).add(inputTax).compareTo(total) != 0)
            throw ApiException.badRequest("Invoice line totals plus tax must equal the invoice total.");

        List<JournalService.Line> journalLines = new ArrayList<>();
        if (toClearing.signum() > 0) {
            journalLines.add(new JournalService.Line(
                    journals.accountIdByCode("1400"), toClearing, BigDecimal.ZERO,
                    "GRNI cleared - " + invoice.invoiceNumber));
        }
        if (toExpense.signum() > 0) {
            for (PurInvoiceLine line : lines) if (line.receiptLineId == null && nz(line.lineTotal).signum() > 0) {
                Long expenseAccount = line.expenseAccountId == null ? journals.accountIdByCode("6000") : line.expenseAccountId;
                journalLines.add(new JournalService.Line(expenseAccount, nz(line.lineTotal), BigDecimal.ZERO, "Expense - " + invoice.invoiceNumber));
            }
        }
        if (inputTax.signum() > 0) journalLines.add(new JournalService.Line(journals.accountIdByCode("1500"), inputTax, BigDecimal.ZERO, "Input tax"));
        journalLines.add(new JournalService.Line(
                journals.accountIdByCode("2100"), BigDecimal.ZERO, total, "AP - " + invoice.invoiceNumber));

        var entry = journals.post("VENDOR_INVOICE", invoiceId, invoice.invoiceDate,
                "Vendor invoice " + invoice.invoiceNumber, journalLines, invoice.currency);

        invoice.status = VendorInvoiceStatus.POSTED;
        invoice.balanceAmount = total;
        invoice.journalEntryId = entry.id;
        audit.custom("POST", "PurInvoice", invoice.id, "Posted vendor invoice " + invoice.invoiceNumber);
        em.merge(invoice);
        return invoice;
    }

    /** Releases a hold so an out-of-tolerance invoice can be posted deliberately. */
    @Transactional
    public PurInvoice releaseHold(Long invoiceId) {
        PurInvoice invoice = records.lock(PurInvoice.class, invoiceId);
        if (invoice == null) {
            throw ApiException.notFound("Vendor invoice " + invoiceId + " was not found.");
        }
        if (invoice.status != VendorInvoiceStatus.ON_HOLD) {
            throw ApiException.conflict("Invoice " + invoice.invoiceNumber + " is not on hold.");
        }
        invoice.status = VendorInvoiceStatus.APPROVED;
        invoice.holdReason = null;
        audit.custom("RELEASE_HOLD", "PurInvoice", invoice.id, "Approved posting outside match tolerance.");
        em.merge(invoice);
        return invoice;
    }

    /**
     * Compares each invoice line with its PO and goods receipt lines (M-PUR-005).
     * Tolerances come from the match record defaults and can be tuned per line.
     */
    @Transactional
    public MatchStatus runThreeWayMatch(PurInvoice invoice) {
        invoice = records.lock(PurInvoice.class, invoice.id);
        List<PurInvoiceLine> lines = em.createQuery(
                "select l from PurInvoiceLine l where l.active = true and l.invoiceId = :i order by l.lineNo",
                PurInvoiceLine.class)
                .setParameter("i", invoice.id)
                .getResultList();

        em.createQuery("delete from PurThreeWayMatch m where m.invoiceId = :i")
                .setParameter("i", invoice.id)
                .executeUpdate();

        if (lines.isEmpty()) {
            return MatchStatus.PENDING;
        }

        boolean quantityVariance = false;
        boolean priceVariance = false;
        boolean anyMatched = false;

        for (PurInvoiceLine line : lines) {
            com.erp.shared.RecordPolicy.owned(line, invoice.companyId);
            if (nz(line.quantity).signum() <= 0 || nz(line.unitPrice).signum() < 0 || nz(line.lineTotal).signum() < 0)
                throw ApiException.badRequest("Invoice lines require positive quantities and non-negative prices and totals.");
            if (line.orderLineId == null) {
                continue;
            }
            PurOrderLine orderLine = records.lock(PurOrderLine.class, line.orderLineId);
            if (orderLine == null) {
                continue;
            }
            anyMatched = true;

            PurThreeWayMatch match = new PurThreeWayMatch();
            match.invoiceId = invoice.id;
            match.invoiceLineId = line.id;
            match.orderLineId = orderLine.id;
            match.itemId = line.itemId;
            match.orderedQuantity = nz(orderLine.quantity);
            match.receivedQuantity = nz(orderLine.receivedQuantity);
            match.invoicedQuantity = nz(line.quantity);
            match.orderedPrice = nz(orderLine.unitPrice);
            match.invoicedPrice = nz(line.unitPrice);
            match.quantityTolerancePercent = DEFAULT_QTY_TOLERANCE;
            match.priceTolerancePercent = DEFAULT_PRICE_TOLERANCE;

            BigDecimal qtyDiff = nz(line.quantity).subtract(nz(orderLine.receivedQuantity)).max(BigDecimal.ZERO);
            BigDecimal qtyVariancePct = percentage(qtyDiff, nz(orderLine.receivedQuantity));
            BigDecimal priceDiff = nz(line.unitPrice).subtract(nz(orderLine.unitPrice)).abs();
            BigDecimal priceVariancePct = percentage(priceDiff, nz(orderLine.unitPrice));

            match.quantityVariance = qtyVariancePct;
            match.priceVariance = priceVariancePct;

            boolean qtyOk = qtyVariancePct.compareTo(DEFAULT_QTY_TOLERANCE) <= 0;
            boolean priceOk = priceVariancePct.compareTo(DEFAULT_PRICE_TOLERANCE) <= 0;
            quantityVariance |= !qtyOk;
            priceVariance |= !priceOk;

            match.withinTolerance = qtyOk && priceOk;
            match.matchStatus = qtyOk && priceOk ? MatchStatus.MATCHED
                    : (!qtyOk && !priceOk ? MatchStatus.BOTH_VARIANCE
                    : (qtyOk ? MatchStatus.PRICE_VARIANCE : MatchStatus.QUANTITY_VARIANCE));
            match.companyId = invoice.companyId;
            em.persist(match);
        }

        if (!anyMatched) {
            return MatchStatus.NO_MATCH;
        }
        if (quantityVariance && priceVariance) {
            return MatchStatus.BOTH_VARIANCE;
        }
        if (quantityVariance) {
            return MatchStatus.QUANTITY_VARIANCE;
        }
        if (priceVariance) {
            return MatchStatus.PRICE_VARIANCE;
        }
        return MatchStatus.MATCHED;
    }

    private void recalculateOrderStatus(Long orderId) {
        PurOrder order = records.lock(PurOrder.class, orderId);
        if (order == null || order.status == PoStatus.CANCELLED || order.status == PoStatus.CLOSED) {
            return;
        }
        List<PurOrderLine> lines = em.createQuery(
                "select l from PurOrderLine l where l.active = true and l.orderId = :o", PurOrderLine.class)
                .setParameter("o", orderId)
                .getResultList();

        boolean fullyReceived = !lines.isEmpty();
        for (PurOrderLine line : lines) {
            if (nz(line.receivedQuantity).compareTo(nz(line.quantity)) < 0) {
                fullyReceived = false;
                break;
            }
        }
        order.status = fullyReceived ? PoStatus.RECEIVED : PoStatus.PARTIALLY_RECEIVED;
        em.merge(order);
    }

    private static BigDecimal percentage(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.signum() == 0) {
            return part.signum() == 0 ? BigDecimal.ZERO : new BigDecimal("100");
        }
        return part.multiply(new BigDecimal("100")).divide(whole, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
