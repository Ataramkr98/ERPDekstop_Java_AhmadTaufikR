package com.erp.purchasing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Vendor invoice, the AP document (M-PUR-008, M-FIN-003). Entered manually, imported by OCR, or
 * ingested as an e-invoice.
 */
@Entity(name = "PurInvoice")
@Table(name = "pur_invoice")
public class PurInvoice extends BaseEntity {

    /**
     * invoiceNumber
     */
    @Column(name = "invoice_number", length = 80, nullable = false)
    public String invoiceNumber;

    /**
     * The vendor's own number, used for duplicate detection.
     */
    @Column(name = "vendor_invoice_number", length = 80)
    public String vendorInvoiceNumber;

    /**
     * vendorId
     */
    @Column(name = "vendor_id", nullable = false)
    public Long vendorId;

    /**
     * orderId
     */
    @Column(name = "order_id")
    public Long orderId;

    /**
     * receiptId
     */
    @Column(name = "receipt_id")
    public Long receiptId;

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
    public VendorInvoiceStatus status = VendorInvoiceStatus.DRAFT;

    /**
     * matchStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", length = 40)
    public MatchStatus matchStatus;

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
     * Field-level confidence from the OCR pipeline (M-PUR-010).
     */
    @Column(name = "ocr_confidence", length = 20)
    public String ocrConfidence;

    /**
     * Populated when the three-way match puts the invoice on hold.
     */
    @Column(name = "hold_reason")
    public String holdReason;

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
