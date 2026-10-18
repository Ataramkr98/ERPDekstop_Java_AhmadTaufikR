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
 * Customer invoice, the AR document (M-FIN-004). Generated from a delivery, a project milestone
 * or entered manually.
 */
@Entity(name = "SalInvoice")
@Table(name = "sal_invoice")
public class SalInvoice extends BaseEntity {

    /**
     * invoiceNumber
     */
    @Column(name = "invoice_number", length = 40, nullable = false)
    public String invoiceNumber;

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
     * deliveryId
     */
    @Column(name = "delivery_id")
    public Long deliveryId;

    /**
     * invoiceDate
     */
    @Column(name = "invoice_date", nullable = false)
    public LocalDate invoiceDate;

    /**
     * dueDate
     */
    @Column(name = "due_date")
    public LocalDate dueDate;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public InvoiceStatus status = InvoiceStatus.DRAFT;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * exchangeRate
     */
    @Column(name = "exchange_rate", precision = 9, scale = 6)
    public BigDecimal exchangeRate;

    /**
     * subtotal
     */
    @Column(name = "subtotal", precision = 19, scale = 4)
    public BigDecimal subtotal;

    /**
     * discountAmount
     */
    @Column(name = "discount_amount", precision = 19, scale = 4)
    public BigDecimal discountAmount;

    /**
     * taxAmount
     */
    @Column(name = "tax_amount", precision = 19, scale = 4)
    public BigDecimal taxAmount;

    /**
     * totalAmount
     */
    @Column(name = "total_amount", precision = 19, scale = 4)
    public BigDecimal totalAmount;

    /**
     * paidAmount
     */
    @Column(name = "paid_amount", precision = 19, scale = 4)
    public BigDecimal paidAmount;

    /**
     * balanceAmount
     */
    @Column(name = "balance_amount", precision = 19, scale = 4)
    public BigDecimal balanceAmount;

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

    /**
     * DELIVERY, MILESTONE, MANUAL, SERVICE.
     */
    @Column(name = "source_type", length = 30)
    public String sourceType;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return invoiceNumber;
    }

}
