package com.erp.manufacturing;

import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One MRP regeneration (M-MFG-004). Fully re-runnable; exceptions are listed in priority order
 * and plan orders convert to POs or work orders.
 */
@Entity(name = "MfgMrpRun")
@Table(name = "mfg_mrp_run")
public class MfgMrpRun extends BaseEntity {

    /**
     * runDate
     */
    @Column(name = "run_date", nullable = false)
    public LocalDate runDate;

    /**
     * status
     */
    @Column(name = "status", length = 20)
    public String status;

    /**
     * itemCount
     */
    @Column(name = "item_count")
    public Integer itemCount;

    /**
     * exceptionCount
     */
    @Column(name = "exception_count")
    public Integer exceptionCount;

    /**
     * plannedOrderCount
     */
    @Column(name = "planned_order_count")
    public Integer plannedOrderCount;

    /**
     * horizonEndDate
     */
    @Column(name = "horizon_end_date")
    public LocalDate horizonEndDate;

    /**
     * runBy
     */
    @Column(name = "run_by", length = 120)
    public String runBy;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return status;
    }

}
