package com.erp.sales;

import java.math.BigDecimal;
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
 * Order-to-cash posting (M-SAL-008, M-FIN-004).
 *
 * <p>Shipping a delivery does two things that must succeed or fail together: it writes
 * the stock movements and it posts the COGS journal. Posting an invoice raises the AR
 * document and the revenue recognition in the same transaction.
 *
 * <p>Keeping this in a service rather than in the resource is what stops a caller from
 * creating stock movements without the matching ledger entries.
 */
@ApplicationScoped
public class SalesService {

    @Inject
    EntityManager em;
    @Inject com.erp.shared.BusinessRecords records;
    @Inject com.erp.platform.AuditService audit;

    @Inject
    JournalService journals;

    @Inject
    InventoryService inventory;

    /**
     * Ship confirms a delivery: decrements stock per line and posts
     * DR Cost of Goods Sold / CR Inventory for the value of the goods.
     */
    @Transactional
    public SalDelivery ship(Long deliveryId) {
        SalDelivery delivery = records.lock(SalDelivery.class, deliveryId);
        if (delivery == null) {
            throw ApiException.notFound("Delivery " + deliveryId + " was not found.");
        }
        if (delivery.status == DeliveryStatus.SHIPPED || delivery.status == DeliveryStatus.DELIVERED) {
            throw ApiException.conflict("Delivery " + delivery.deliveryNumber + " has already been shipped.");
        }
        if (delivery.status == DeliveryStatus.CANCELLED) {
            throw ApiException.conflict("Delivery " + delivery.deliveryNumber + " was cancelled.");
        }

        List<SalDeliveryLine> lines = em.createQuery(
                "select l from SalDeliveryLine l where l.active = true and l.deliveryId = :d order by l.lineNo",
                SalDeliveryLine.class)
                .setParameter("d", deliveryId)
                .getResultList();
        if (lines.isEmpty()) {
            throw ApiException.badRequest("Delivery " + delivery.deliveryNumber
                    + " has no lines. Add at least one line before shipping.");
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        for (SalDeliveryLine line : lines) {
            com.erp.shared.RecordPolicy.owned(line, delivery.companyId);
            if (nz(line.quantity).signum() <= 0) throw ApiException.badRequest("Delivery quantities must be positive.");
            InventoryService.MovementRequest move = new InventoryService.MovementRequest(
                    MovementType.SHIP, line.itemId, line.warehouseId,
                    nz(line.quantity).negate());
            move.locationId = null; // Delivery lines have no storage-location field.
            move.lotId = line.lotId;
            move.uom = line.uom;
            move.sourceDocumentType = "SALES_DELIVERY";
            move.sourceDocumentId = deliveryId;
            move.movementDate = delivery.deliveryDate;
            move.notes = "Delivery " + delivery.deliveryNumber;

            totalCost = totalCost.add(inventory.record(move).totalCost);

            if (line.orderLineId != null) {
                SalOrderLine orderLine = records.lock(SalOrderLine.class, line.orderLineId);
                if (orderLine != null) {
                    orderLine.deliveredQuantity = nz(orderLine.deliveredQuantity).add(nz(line.quantity));
                    em.merge(orderLine);
                }
            }
        }

        List<JournalService.Line> journalLines = new ArrayList<>();
        journalLines.add(new JournalService.Line(
                journals.accountIdByCode("5000"), totalCost, BigDecimal.ZERO, "COGS - " + delivery.deliveryNumber));
        journalLines.add(new JournalService.Line(
                journals.accountIdByCode("1300"), BigDecimal.ZERO, totalCost, "Inventory issued - " + delivery.deliveryNumber));

        var entry = journals.post("SALES_DELIVERY", deliveryId, delivery.deliveryDate,
                "Cost of goods sold for delivery " + delivery.deliveryNumber, journalLines);

        delivery.status = DeliveryStatus.SHIPPED;
        delivery.journalEntryId = entry.id;
        audit.custom("SHIP", "SalDelivery", delivery.id, "Shipped delivery " + delivery.deliveryNumber);
        em.merge(delivery);

        if (delivery.orderId != null) {
            recalculateOrderStatus(delivery.orderId);
        }
        return delivery;
    }

    /**
     * Posts a customer invoice: DR Accounts Receivable against CR Sales Revenue and
     * CR Tax Payable, then updates the customer's open balance.
     */
    @Transactional
    public SalInvoice postInvoice(Long invoiceId) {
        SalInvoice invoice = records.lock(SalInvoice.class, invoiceId);
        records.lock(SalCustomer.class, invoice.customerId);
        if (invoice == null) {
            throw ApiException.notFound("Invoice " + invoiceId + " was not found.");
        }
        if (invoice.journalEntryId != null || invoice.status == InvoiceStatus.POSTED || invoice.status == InvoiceStatus.PAID) {
            throw ApiException.conflict("Invoice " + invoice.invoiceNumber + " has already been posted.");
        }
        if (invoice.status == InvoiceStatus.VOID) {
            throw ApiException.conflict("Invoice " + invoice.invoiceNumber + " is void.");
        }

        BigDecimal total = nz(invoice.totalAmount);
        BigDecimal tax = nz(invoice.taxAmount);
        BigDecimal net = total.subtract(tax);
        if (tax.signum() < 0) throw ApiException.badRequest("Tax cannot be negative.");
        List<SalInvoiceLine> invoiceLines = em.createQuery("select l from SalInvoiceLine l where l.invoiceId=:id and l.active=true", SalInvoiceLine.class).setParameter("id", invoice.id).getResultList();
        if (invoiceLines.isEmpty()) throw ApiException.badRequest("Add invoice lines before posting.");
        BigDecimal lineNet = BigDecimal.ZERO;
        for (SalInvoiceLine line : invoiceLines) {
            com.erp.shared.RecordPolicy.owned(line, invoice.companyId);
            if (nz(line.quantity).signum() <= 0 || nz(line.lineTotal).signum() < 0) throw ApiException.badRequest("Invoice lines need positive quantities and non-negative totals.");
            lineNet = lineNet.add(nz(line.lineTotal));
        }
        if (lineNet.compareTo(net) != 0) throw ApiException.badRequest("Invoice line totals plus tax must equal the invoice total.");
        if (total.signum() <= 0) {
            throw ApiException.badRequest("Invoice " + invoice.invoiceNumber
                    + " has no value. Add lines before posting.");
        }
        if (net.signum() < 0) {
            throw ApiException.badRequest("Tax cannot be larger than the invoice total on "
                    + invoice.invoiceNumber + ".");
        }

        List<JournalService.Line> lines = new ArrayList<>();
        lines.add(new JournalService.Line(
                journals.accountIdByCode("1200"), total, BigDecimal.ZERO, "AR - " + invoice.invoiceNumber));
        lines.add(new JournalService.Line(
                journals.accountIdByCode("4000"), BigDecimal.ZERO, net, "Revenue - " + invoice.invoiceNumber));
        if (tax.signum() > 0) {
            lines.add(new JournalService.Line(
                    journals.accountIdByCode("2200"), BigDecimal.ZERO, tax, "Tax - " + invoice.invoiceNumber));
        }

        var entry = journals.post("CUSTOMER_INVOICE", invoiceId, invoice.invoiceDate,
                "Customer invoice " + invoice.invoiceNumber, lines, invoice.currency);

        invoice.status = InvoiceStatus.POSTED;
        invoice.balanceAmount = total;
        invoice.journalEntryId = entry.id;
        audit.custom("POST", "SalInvoice", invoice.id, "Posted invoice " + invoice.invoiceNumber);
        em.merge(invoice);

        if (invoice.customerId != null) {
            var customer = records.lock(SalCustomer.class, invoice.customerId);
            if (customer != null) {
                customer.currentBalance = nz(customer.currentBalance).add(total);
                em.merge(customer);
            }
        }
        return invoice;
    }

    /** Rolls the order status up from its lines once a delivery or invoice is posted. */
    private void recalculateOrderStatus(Long orderId) {
        SalOrder order = records.lock(SalOrder.class, orderId);
        if (order == null || order.status == SalesOrderStatus.CANCELLED
                || order.status == SalesOrderStatus.CLOSED) {
            return;
        }
        List<SalOrderLine> lines = em.createQuery(
                "select l from SalOrderLine l where l.active = true and l.orderId = :o", SalOrderLine.class)
                .setParameter("o", orderId)
                .getResultList();

        boolean fullyDelivered = !lines.isEmpty();
        for (SalOrderLine line : lines) {
            if (nz(line.deliveredQuantity).compareTo(nz(line.quantity)) < 0) {
                fullyDelivered = false;
                break;
            }
        }
        order.status = fullyDelivered ? SalesOrderStatus.DELIVERED : SalesOrderStatus.PARTIALLY_DELIVERED;
        em.merge(order);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
