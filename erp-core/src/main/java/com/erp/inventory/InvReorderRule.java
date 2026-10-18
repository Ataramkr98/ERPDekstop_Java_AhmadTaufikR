package com.erp.inventory;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Replenishment rule per item and warehouse (M-INV-009). The proposal run turns shortages into
 * requisitions or purchase orders depending on configuration.
 */
@Entity(name = "InvReorderRule")
@Table(name = "inv_reorder_rule")
public class InvReorderRule extends BaseEntity {

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id", nullable = false)
    public Long warehouseId;

    /**
     * reorderPoint
     */
    @Column(name = "reorder_point", precision = 9, scale = 6)
    public BigDecimal reorderPoint;

    /**
     * safetyStock
     */
    @Column(name = "safety_stock", precision = 9, scale = 6)
    public BigDecimal safetyStock;

    /**
     * minQuantity
     */
    @Column(name = "min_quantity", precision = 9, scale = 6)
    public BigDecimal minQuantity;

    /**
     * maxQuantity
     */
    @Column(name = "max_quantity", precision = 9, scale = 6)
    public BigDecimal maxQuantity;

    /**
     * reorderQuantity
     */
    @Column(name = "reorder_quantity", precision = 9, scale = 6)
    public BigDecimal reorderQuantity;

    /**
     * leadTimeDays
     */
    @Column(name = "lead_time_days")
    public Integer leadTimeDays;

    /**
     * abcClass
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "abc_class", length = 40)
    public AbcClass abcClass;

    /**
     * preferredVendorId
     */
    @Column(name = "preferred_vendor_id")
    public Long preferredVendorId;

    /**
     * autoGenerate
     */
    @Column(name = "auto_generate")
    public Boolean autoGenerate;

}
