package com.erp.purchasing;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Received quantity per order line. Over-receipt beyond tolerance is blocked or warned per
 * configuration (M-PUR-004).
 */
@Entity(name = "PurGoodsReceiptLine")
@Table(name = "pur_goods_receipt_line")
public class PurGoodsReceiptLine extends BaseEntity {

    /**
     * receiptId
     */
    @Column(name = "receipt_id", nullable = false)
    public Long receiptId;

    /**
     * lineNo
     */
    @Column(name = "line_no", nullable = false)
    public Integer lineNo;

    /**
     * orderLineId
     */
    @Column(name = "order_line_id")
    public Long orderLineId;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * quantity
     */
    @Column(name = "quantity", precision = 9, scale = 6)
    public BigDecimal quantity;

    /**
     * orderedQuantity
     */
    @Column(name = "ordered_quantity", precision = 9, scale = 6)
    public BigDecimal orderedQuantity;

    /**
     * overReceiptPercent
     */
    @Column(name = "over_receipt_percent", precision = 9, scale = 6)
    public BigDecimal overReceiptPercent;

    /**
     * uom
     */
    @Column(name = "uom", length = 20)
    public String uom;

    /**
     * unitCost
     */
    @Column(name = "unit_cost", precision = 19, scale = 4)
    public BigDecimal unitCost;

    /**
     * lineTotal
     */
    @Column(name = "line_total", precision = 19, scale = 4)
    public BigDecimal lineTotal;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * locationId
     */
    @Column(name = "location_id")
    public Long locationId;

    /**
     * lotId
     */
    @Column(name = "lot_id")
    public Long lotId;

    /**
     * qualityStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "quality_status", length = 40)
    public QualityStatus qualityStatus;

}
