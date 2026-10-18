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
 * Non-conformance report with CAPA tracking (M-MFG-007).
 */
@Entity(name = "MfgNonConformance")
@Table(name = "mfg_non_conformance")
public class MfgNonConformance extends BaseEntity {

    /**
     * ncrNumber
     */
    @Column(name = "ncr_number", length = 40, nullable = false)
    public String ncrNumber;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * workOrderId
     */
    @Column(name = "work_order_id")
    public Long workOrderId;

    /**
     * inspectionLotId
     */
    @Column(name = "inspection_lot_id")
    public Long inspectionLotId;

    /**
     * vendorId
     */
    @Column(name = "vendor_id")
    public Long vendorId;

    /**
     * raisedDate
     */
    @Column(name = "raised_date", nullable = false)
    public LocalDate raisedDate;

    /**
     * defectCode
     */
    @Column(name = "defect_code", length = 40)
    public String defectCode;

    /**
     * severity
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 40)
    public NcrSeverity severity;

    /**
     * quantityAffected
     */
    @Column(name = "quantity_affected", precision = 9, scale = 6)
    public BigDecimal quantityAffected;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * rootCause
     */
    @Column(name = "root_cause")
    public String rootCause;

    /**
     * correctiveAction
     */
    @Column(name = "corrective_action")
    public String correctiveAction;

    /**
     * preventiveAction
     */
    @Column(name = "preventive_action")
    public String preventiveAction;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public NcrStatus status;

    /**
     * assignedTo
     */
    @Column(name = "assigned_to", length = 120)
    public String assignedTo;

    /**
     * dueDate
     */
    @Column(name = "due_date")
    public LocalDate dueDate;

    /**
     * closedDate
     */
    @Column(name = "closed_date")
    public LocalDate closedDate;

    @Override
    public String displayLabel() {
        return ncrNumber;
    }

}
