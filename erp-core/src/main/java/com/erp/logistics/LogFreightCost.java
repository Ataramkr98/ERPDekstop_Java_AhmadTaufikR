package com.erp.logistics;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Freight cost accrued at shipment and reconciled against the carrier invoice (M-LOG-004).
 * Variance between accrual and invoice is reported.
 */
@Entity(name = "LogFreightCost")
@Table(name = "log_freight_cost")
public class LogFreightCost extends BaseEntity {

    /**
     * shipmentId
     */
    @Column(name = "shipment_id", nullable = false)
    public Long shipmentId;

    /**
     * carrierId
     */
    @Column(name = "carrier_id")
    public Long carrierId;

    /**
     * BASE, FUEL_SURCHARGE, INSURANCE, DUTY, HANDLING, OTHER.
     */
    @Column(name = "cost_type", length = 40)
    public String costType;

    /**
     * accruedAmount
     */
    @Column(name = "accrued_amount", precision = 19, scale = 4)
    public BigDecimal accruedAmount;

    /**
     * invoicedAmount
     */
    @Column(name = "invoiced_amount", precision = 19, scale = 4)
    public BigDecimal invoicedAmount;

    /**
     * varianceAmount
     */
    @Column(name = "variance_amount", precision = 19, scale = 4)
    public BigDecimal varianceAmount;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public FreightStatus status;

    /**
     * vendorInvoiceId
     */
    @Column(name = "vendor_invoice_id")
    public Long vendorInvoiceId;

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

    /**
     * accrualDate
     */
    @Column(name = "accrual_date")
    public LocalDate accrualDate;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
