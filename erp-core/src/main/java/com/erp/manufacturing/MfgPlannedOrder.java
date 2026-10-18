package com.erp.manufacturing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Planned supply order from MRP (M-MFG-004). Converts into a purchase order or a work order
 * without losing the originating demand reference.
 */
@Entity(name = "MfgPlannedOrder")
@Table(name = "mfg_planned_order")
public class MfgPlannedOrder extends BaseEntity {

    /**
     * mrpRunId
     */
    @Column(name = "mrp_run_id")
    public Long mrpRunId;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * requiredDate
     */
    @Column(name = "required_date", nullable = false)
    public LocalDate requiredDate;

    /**
     * requiredQuantity
     */
    @Column(name = "required_quantity", precision = 9, scale = 6)
    public BigDecimal requiredQuantity;

    /**
     * orderType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 40)
    public PlannedOrderType orderType;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public PlannedOrderStatus status;

    /**
     * SALES_ORDER, FORECAST, SAFETY_STOCK, REORDER_RULE.
     */
    @Column(name = "source_type", length = 30)
    public String sourceType;

    /**
     * sourceDocumentId
     */
    @Column(name = "source_document_id")
    public Long sourceDocumentId;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * vendorId
     */
    @Column(name = "vendor_id")
    public Long vendorId;

    /**
     * convertedOrderId
     */
    @Column(name = "converted_order_id")
    public Long convertedOrderId;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
