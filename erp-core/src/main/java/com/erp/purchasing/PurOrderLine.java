package com.erp.purchasing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Purchase order line (M-PUR-003). receivedQuantity and invoicedQuantity drive the three-way
 * match tolerances.
 */
@Entity(name = "PurOrderLine")
@Table(name = "pur_order_line")
public class PurOrderLine extends BaseEntity {

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
     * requiredDate
     */
    @Column(name = "required_date")
    public LocalDate requiredDate;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * expenseAccountId
     */
    @Column(name = "expense_account_id")
    public Long expenseAccountId;

}
