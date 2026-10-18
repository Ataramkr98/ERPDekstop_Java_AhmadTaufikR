package com.erp.purchasing;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Purchase order header (M-PUR-003). Created from an approved requisition, an RFQ award, or
 * directly; every revision is tracked.
 */
@Entity(name = "PurOrder")
@Table(name = "pur_order")
public class PurOrder extends BaseEntity {

    /**
     * orderNumber
     */
    @Column(name = "order_number", length = 40, nullable = false)
    public String orderNumber;

    /**
     * vendorId
     */
    @Column(name = "vendor_id", nullable = false)
    public Long vendorId;

    /**
     * orderDate
     */
    @Column(name = "order_date", nullable = false)
    public LocalDate orderDate;

    /**
     * requiredDate
     */
    @Column(name = "required_date")
    public LocalDate requiredDate;

    /**
     * promisedDate
     */
    @Column(name = "promised_date")
    public LocalDate promisedDate;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public PoStatus status;

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
     * discountAmount
     */
    @Column(name = "discount_amount", precision = 19, scale = 4)
    public BigDecimal discountAmount;

    /**
     * totalAmount
     */
    @Column(name = "total_amount", precision = 19, scale = 4)
    public BigDecimal totalAmount;

    /**
     * receivedAmount
     */
    @Column(name = "received_amount", precision = 19, scale = 4)
    public BigDecimal receivedAmount;

    /**
     * invoicedAmount
     */
    @Column(name = "invoiced_amount", precision = 19, scale = 4)
    public BigDecimal invoicedAmount;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * requisitionId
     */
    @Column(name = "requisition_id")
    public Long requisitionId;

    /**
     * rfqId
     */
    @Column(name = "rfq_id")
    public Long rfqId;

    /**
     * buyerEmployeeId
     */
    @Column(name = "buyer_employee_id")
    public Long buyerEmployeeId;

    /**
     * revision
     */
    @Column(name = "revision")
    public Integer revision;

    /**
     * approvedBy
     */
    @Column(name = "approved_by", length = 120)
    public String approvedBy;

    /**
     * approvedAt
     */
    @Column(name = "approved_at")
    public Instant approvedAt;

    /**
     * paymentTerms
     */
    @Column(name = "payment_terms")
    public String paymentTerms;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return orderNumber;
    }

}
