package com.erp.manufacturing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Component issued to a work order (M-MFG-006). Backflush issues are generated automatically on
 * completion.
 */
@Entity(name = "MfgMaterialIssue")
@Table(name = "mfg_material_issue")
public class MfgMaterialIssue extends BaseEntity {

    /**
     * workOrderId
     */
    @Column(name = "work_order_id", nullable = false)
    public Long workOrderId;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

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
     * unitCost
     */
    @Column(name = "unit_cost", precision = 19, scale = 4)
    public BigDecimal unitCost;

    /**
     * totalCost
     */
    @Column(name = "total_cost", precision = 19, scale = 4)
    public BigDecimal totalCost;

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
     * Inventory movement created by the issue.
     */
    @Column(name = "movement_id")
    public Long movementId;

    /**
     * backflushed
     */
    @Column(name = "backflushed")
    public Boolean backflushed;

    /**
     * issueDate
     */
    @Column(name = "issue_date")
    public LocalDate issueDate;

}
