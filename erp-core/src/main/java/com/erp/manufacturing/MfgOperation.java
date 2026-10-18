package com.erp.manufacturing;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One operation in a routing (M-MFG-002). A subcontracted operation raises a purchase order to
 * the subcontractor (M-MFG-009).
 */
@Entity(name = "MfgOperation")
@Table(name = "mfg_operation")
public class MfgOperation extends BaseEntity {

    /**
     * routingId
     */
    @Column(name = "routing_id", nullable = false)
    public Long routingId;

    /**
     * operationNo
     */
    @Column(name = "operation_no", nullable = false)
    public Integer operationNo;

    /**
     * workCenterId
     */
    @Column(name = "work_center_id")
    public Long workCenterId;

    /**
     * description
     */
    @Column(name = "description", length = 300)
    public String description;

    /**
     * setupTimeMinutes
     */
    @Column(name = "setup_time_minutes", precision = 9, scale = 6)
    public BigDecimal setupTimeMinutes;

    /**
     * runTimeMinutesPerUnit
     */
    @Column(name = "run_time_minutes_per_unit", precision = 9, scale = 6)
    public BigDecimal runTimeMinutesPerUnit;

    /**
     * teardownTimeMinutes
     */
    @Column(name = "teardown_time_minutes", precision = 9, scale = 6)
    public BigDecimal teardownTimeMinutes;

    /**
     * queueTimeHours
     */
    @Column(name = "queue_time_hours", precision = 9, scale = 6)
    public BigDecimal queueTimeHours;

    /**
     * isSubcontracted
     */
    @Column(name = "is_subcontracted")
    public Boolean isSubcontracted;

    /**
     * subcontractorVendorId
     */
    @Column(name = "subcontractor_vendor_id")
    public Long subcontractorVendorId;

    /**
     * subcontractCost
     */
    @Column(name = "subcontract_cost", precision = 19, scale = 4)
    public BigDecimal subcontractCost;

    /**
     * Milestone operations trigger progress billing.
     */
    @Column(name = "is_milestone")
    public Boolean isMilestone;

}
