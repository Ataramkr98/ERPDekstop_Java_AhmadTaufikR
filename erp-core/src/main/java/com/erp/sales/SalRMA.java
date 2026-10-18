package com.erp.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Return merchandise authorisation (M-SAL-009). Linked to the original delivery or invoice
 * line; the disposition drives a credit memo or a replacement order.
 */
@Entity(name = "SalRMA")
@Table(name = "sal_rma")
public class SalRMA extends BaseEntity {

    /**
     * rmaNumber
     */
    @Column(name = "rma_number", length = 40, nullable = false)
    public String rmaNumber;

    /**
     * customerId
     */
    @Column(name = "customer_id", nullable = false)
    public Long customerId;

    /**
     * orderId
     */
    @Column(name = "order_id")
    public Long orderId;

    /**
     * invoiceId
     */
    @Column(name = "invoice_id")
    public Long invoiceId;

    /**
     * deliveryId
     */
    @Column(name = "delivery_id")
    public Long deliveryId;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * rmaDate
     */
    @Column(name = "rma_date", nullable = false)
    public LocalDate rmaDate;

    /**
     * DAMAGED, WRONG_ITEM, DEFECTIVE, NOT_AS_DESCRIBED, OTHER.
     */
    @Column(name = "reason_code", length = 40)
    public String reasonCode;

    /**
     * quantity
     */
    @Column(name = "quantity", precision = 9, scale = 6)
    public BigDecimal quantity;

    /**
     * disposition
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", length = 40)
    public RmaDisposition disposition;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public RmaStatus status;

    /**
     * replacementOrderId
     */
    @Column(name = "replacement_order_id")
    public Long replacementOrderId;

    /**
     * creditMemoInvoiceId
     */
    @Column(name = "credit_memo_invoice_id")
    public Long creditMemoInvoiceId;

    /**
     * Service ticket that raised this RMA (M-SVC-006).
     */
    @Column(name = "service_ticket_id")
    public Long serviceTicketId;

    /**
     * resolutionNotes
     */
    @Column(name = "resolution_notes")
    public String resolutionNotes;

    @Override
    public String displayLabel() {
        return rmaNumber;
    }

}
