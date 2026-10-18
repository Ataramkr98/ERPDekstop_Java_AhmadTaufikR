package com.erp.purchasing;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Tolerance check between PO, goods receipt and vendor invoice (M-PUR-005). A breach posts a
 * hold with a reason the AP clerk can act on.
 */
@Entity(name = "PurThreeWayMatch")
@Table(name = "pur_three_way_match")
public class PurThreeWayMatch extends BaseEntity {

    /**
     * invoiceId
     */
    @Column(name = "invoice_id", nullable = false)
    public Long invoiceId;

    /**
     * invoiceLineId
     */
    @Column(name = "invoice_line_id")
    public Long invoiceLineId;

    /**
     * orderLineId
     */
    @Column(name = "order_line_id")
    public Long orderLineId;

    /**
     * receiptLineId
     */
    @Column(name = "receipt_line_id")
    public Long receiptLineId;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * orderedQuantity
     */
    @Column(name = "ordered_quantity", precision = 9, scale = 6)
    public BigDecimal orderedQuantity;

    /**
     * receivedQuantity
     */
    @Column(name = "received_quantity", precision = 9, scale = 6)
    public BigDecimal receivedQuantity;

    /**
     * invoicedQuantity
     */
    @Column(name = "invoiced_quantity", precision = 9, scale = 6)
    public BigDecimal invoicedQuantity;

    /**
     * orderedPrice
     */
    @Column(name = "ordered_price", precision = 19, scale = 4)
    public BigDecimal orderedPrice;

    /**
     * invoicedPrice
     */
    @Column(name = "invoiced_price", precision = 19, scale = 4)
    public BigDecimal invoicedPrice;

    /**
     * quantityVariance
     */
    @Column(name = "quantity_variance", precision = 9, scale = 6)
    public BigDecimal quantityVariance;

    /**
     * priceVariance
     */
    @Column(name = "price_variance", precision = 9, scale = 6)
    public BigDecimal priceVariance;

    /**
     * quantityTolerancePercent
     */
    @Column(name = "quantity_tolerance_percent", precision = 9, scale = 6)
    public BigDecimal quantityTolerancePercent;

    /**
     * priceTolerancePercent
     */
    @Column(name = "price_tolerance_percent", precision = 9, scale = 6)
    public BigDecimal priceTolerancePercent;

    /**
     * withinTolerance
     */
    @Column(name = "within_tolerance")
    public Boolean withinTolerance;

    /**
     * matchStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", length = 40)
    public MatchStatus matchStatus;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
