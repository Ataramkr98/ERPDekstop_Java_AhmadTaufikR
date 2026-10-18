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
 * Quotation with revision history (M-SAL-002). One click converts an accepted quote into a
 * sales order carrying the pricing engine result across.
 */
@Entity(name = "SalQuotation")
@Table(name = "sal_quotation")
public class SalQuotation extends BaseEntity {

    /**
     * quoteNumber
     */
    @Column(name = "quote_number", length = 40, nullable = false)
    public String quoteNumber;

    /**
     * customerId
     */
    @Column(name = "customer_id", nullable = false)
    public Long customerId;

    /**
     * quoteDate
     */
    @Column(name = "quote_date", nullable = false)
    public LocalDate quoteDate;

    /**
     * validUntil
     */
    @Column(name = "valid_until")
    public LocalDate validUntil;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public QuotationStatus status;

    /**
     * revision
     */
    @Column(name = "revision")
    public Integer revision;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

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
     * salespersonId
     */
    @Column(name = "salesperson_id")
    public Long salespersonId;

    /**
     * CRM opportunity this quote was raised from (M-CRM-009).
     */
    @Column(name = "opportunity_id")
    public Long opportunityId;

    /**
     * convertedOrderId
     */
    @Column(name = "converted_order_id")
    public Long convertedOrderId;

    /**
     * termsAndConditions
     */
    @Column(name = "terms_and_conditions")
    public String termsAndConditions;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return quoteNumber;
    }

}
