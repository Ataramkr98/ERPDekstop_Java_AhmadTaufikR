package com.erp.sales;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Sales order line (M-SAL-003). Partial delivery and partial invoicing are tracked per line
 * through deliveredQuantity and invoicedQuantity.
 */
@Entity(name = "SalOrderLine")
@Table(name = "sal_order_line")
public class SalOrderLine extends BaseEntity {

    /**
     * orderId
     */
    @Column(name = "order_id", nullable = false)
    public Long orderId;

    /**
     * lineNo
     */
    @Column(name = "line_no", nullable = false)
    public Integer lineNo;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * quantity
     */
    @Column(name = "quantity", precision = 9, scale = 6)
    public BigDecimal quantity;

    /**
     * uom
     */
    @Column(name = "uom", length = 20)
    public String uom;

    /**
     * unitPrice
     */
    @Column(name = "unit_price", precision = 19, scale = 4)
    public BigDecimal unitPrice;

    /**
     * discountPercent
     */
    @Column(name = "discount_percent", precision = 9, scale = 6)
    public BigDecimal discountPercent;

    /**
     * taxCode
     */
    @Column(name = "tax_code", length = 30)
    public String taxCode;

    /**
     * taxAmount
     */
    @Column(name = "tax_amount", precision = 19, scale = 4)
    public BigDecimal taxAmount;

    /**
     * lineTotal
     */
    @Column(name = "line_total", precision = 19, scale = 4)
    public BigDecimal lineTotal;

    /**
     * deliveredQuantity
     */
    @Column(name = "delivered_quantity", precision = 9, scale = 6)
    public BigDecimal deliveredQuantity;

    /**
     * invoicedQuantity
     */
    @Column(name = "invoiced_quantity", precision = 9, scale = 6)
    public BigDecimal invoicedQuantity;

    /**
     * Soft reservation against ATP (M-SAL-007).
     */
    @Column(name = "reserved_quantity", precision = 9, scale = 6)
    public BigDecimal reservedQuantity;

    /**
     * ATP snapshot shown at entry time.
     */
    @Column(name = "available_quantity", precision = 9, scale = 6)
    public BigDecimal availableQuantity;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

}
