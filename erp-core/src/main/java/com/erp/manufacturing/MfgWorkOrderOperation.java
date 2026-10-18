package com.erp.manufacturing;

import java.math.BigDecimal;
import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Operation instance on a work order (M-MFG-006). Confirmation posts the time ticket to labour
 * cost and the good quantity to finished goods.
 */
@Entity(name = "MfgWorkOrderOperation")
@Table(name = "mfg_work_order_operation")
public class MfgWorkOrderOperation extends BaseEntity {

    /**
     * workOrderId
     */
    @Column(name = "work_order_id", nullable = false)
    public Long workOrderId;

    /**
     * operationNo
     */
    @Column(name = "operation_no", nullable = false)
    public Integer operationNo;

    /**
     * operationId
     */
    @Column(name = "operation_id")
    public Long operationId;

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
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public OperationStatus status;

    /**
     * plannedQuantity
     */
    @Column(name = "planned_quantity", precision = 9, scale = 6)
    public BigDecimal plannedQuantity;

    /**
     * completedQuantity
     */
    @Column(name = "completed_quantity", precision = 9, scale = 6)
    public BigDecimal completedQuantity;

    /**
     * scrappedQuantity
     */
    @Column(name = "scrapped_quantity", precision = 9, scale = 6)
    public BigDecimal scrappedQuantity;

    /**
     * actualSetupMinutes
     */
    @Column(name = "actual_setup_minutes", precision = 9, scale = 6)
    public BigDecimal actualSetupMinutes;

    /**
     * actualRunMinutes
     */
    @Column(name = "actual_run_minutes", precision = 9, scale = 6)
    public BigDecimal actualRunMinutes;

    /**
     * laborCost
     */
    @Column(name = "labor_cost", precision = 19, scale = 4)
    public BigDecimal laborCost;

    /**
     * machineCost
     */
    @Column(name = "machine_cost", precision = 19, scale = 4)
    public BigDecimal machineCost;

    /**
     * confirmedBy
     */
    @Column(name = "confirmed_by", length = 120)
    public String confirmedBy;

    /**
     * confirmedAt
     */
    @Column(name = "confirmed_at")
    public Instant confirmedAt;

}
