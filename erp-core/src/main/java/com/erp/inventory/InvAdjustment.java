package com.erp.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Stock adjustment (M-INV-003). Above the configured variance threshold it routes through
 * approval before posting to the inventory account.
 */
@Entity(name = "InvAdjustment")
@Table(name = "inv_adjustment")
public class InvAdjustment extends BaseEntity {

    /**
     * adjustmentNumber
     */
    @Column(name = "adjustment_number", length = 40, nullable = false)
    public String adjustmentNumber;

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
     * adjustmentDate
     */
    @Column(name = "adjustment_date", nullable = false)
    public LocalDate adjustmentDate;

    /**
     * Signed delta applied to on-hand stock.
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
     * totalValue
     */
    @Column(name = "total_value", precision = 19, scale = 4)
    public BigDecimal totalValue;

    /**
     * DAMAGE, THEFT, FOUND, RECLASSIFY, OBSOLESCENCE.
     */
    @Column(name = "reason_code", length = 40)
    public String reasonCode;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public AdjustmentStatus status;

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return adjustmentNumber;
    }

}
