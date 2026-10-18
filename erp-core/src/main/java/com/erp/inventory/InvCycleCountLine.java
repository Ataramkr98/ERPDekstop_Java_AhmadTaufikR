package com.erp.inventory;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One counted line (M-INV-008). Variance is the difference between system and counted quantity,
 * valued at the item's current cost.
 */
@Entity(name = "InvCycleCountLine")
@Table(name = "inv_cycle_count_line")
public class InvCycleCountLine extends BaseEntity {

    /**
     * cycleCountId
     */
    @Column(name = "cycle_count_id", nullable = false)
    public Long cycleCountId;

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
     * systemQuantity
     */
    @Column(name = "system_quantity", precision = 9, scale = 6)
    public BigDecimal systemQuantity;

    /**
     * countedQuantity
     */
    @Column(name = "counted_quantity", precision = 9, scale = 6)
    public BigDecimal countedQuantity;

    /**
     * varianceQuantity
     */
    @Column(name = "variance_quantity", precision = 9, scale = 6)
    public BigDecimal varianceQuantity;

    /**
     * unitCost
     */
    @Column(name = "unit_cost", precision = 19, scale = 4)
    public BigDecimal unitCost;

    /**
     * varianceValue
     */
    @Column(name = "variance_value", precision = 19, scale = 4)
    public BigDecimal varianceValue;

    /**
     * recount
     */
    @Column(name = "recount")
    public Boolean recount;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
