package com.erp.manufacturing;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Work centre with capacity and cost rates (M-MFG-002, M-MFG-005).
 */
@Entity(name = "MfgWorkCenter")
@Table(name = "mfg_work_center")
public class MfgWorkCenter extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 30, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * capacityPerHour
     */
    @Column(name = "capacity_per_hour", precision = 9, scale = 6)
    public BigDecimal capacityPerHour;

    /**
     * hoursPerDay
     */
    @Column(name = "hours_per_day", precision = 9, scale = 6)
    public BigDecimal hoursPerDay;

    /**
     * machineCostPerHour
     */
    @Column(name = "machine_cost_per_hour", precision = 19, scale = 4)
    public BigDecimal machineCostPerHour;

    /**
     * laborCostPerHour
     */
    @Column(name = "labor_cost_per_hour", precision = 19, scale = 4)
    public BigDecimal laborCostPerHour;

    /**
     * Overhead multiplier applied to the cost rollup.
     */
    @Column(name = "overhead_rate", precision = 9, scale = 6)
    public BigDecimal overheadRate;

    /**
     * efficiencyPercent
     */
    @Column(name = "efficiency_percent", precision = 9, scale = 6)
    public BigDecimal efficiencyPercent;

    /**
     * costCenterId
     */
    @Column(name = "cost_center_id")
    public Long costCenterId;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
