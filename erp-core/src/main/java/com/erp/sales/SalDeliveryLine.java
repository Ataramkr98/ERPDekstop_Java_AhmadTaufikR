package com.erp.sales;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Delivered quantity per order line. Posting writes one stock movement per line.
 */
@Entity(name = "SalDeliveryLine")
@Table(name = "sal_delivery_line")
public class SalDeliveryLine extends BaseEntity {

    /**
     * deliveryId
     */
    @Column(name = "delivery_id", nullable = false)
    public Long deliveryId;

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
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * lotId
     */
    @Column(name = "lot_id")
    public Long lotId;

    /**
     * uom
     */
    @Column(name = "uom", length = 20)
    public String uom;

}
