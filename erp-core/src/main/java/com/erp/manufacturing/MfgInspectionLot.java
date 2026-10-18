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
 * Inspection lot with AQL sampling (M-MFG-007). A failed lot gates the stock and raises a
 * non-conformance report.
 */
@Entity(name = "MfgInspectionLot")
@Table(name = "mfg_inspection_lot")
public class MfgInspectionLot extends BaseEntity {

    /**
     * inspectionNumber
     */
    @Column(name = "inspection_number", length = 40, nullable = false)
    public String inspectionNumber;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * lotId
     */
    @Column(name = "lot_id")
    public Long lotId;

    /**
     * workOrderId
     */
    @Column(name = "work_order_id")
    public Long workOrderId;

    /**
     * receiptId
     */
    @Column(name = "receipt_id")
    public Long receiptId;

    /**
     * inspectionDate
     */
    @Column(name = "inspection_date")
    public LocalDate inspectionDate;

    /**
     * INCOMING, IN_PROCESS, FINAL, CUSTOMER_RETURN.
     */
    @Column(name = "inspection_type", length = 30)
    public String inspectionType;

    /**
     * plannedQuantity
     */
    @Column(name = "planned_quantity", precision = 9, scale = 6)
    public BigDecimal plannedQuantity;

    /**
     * sampleSize
     */
    @Column(name = "sample_size", precision = 9, scale = 6)
    public BigDecimal sampleSize;

    /**
     * acceptedQuantity
     */
    @Column(name = "accepted_quantity", precision = 9, scale = 6)
    public BigDecimal acceptedQuantity;

    /**
     * rejectedQuantity
     */
    @Column(name = "rejected_quantity", precision = 9, scale = 6)
    public BigDecimal rejectedQuantity;

    /**
     * aqlLevel
     */
    @Column(name = "aql_level", length = 10)
    public String aqlLevel;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public InspectionStatus status;

    /**
     * result
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 40)
    public InspectionResult result;

    /**
     * inspector
     */
    @Column(name = "inspector", length = 120)
    public String inspector;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return inspectionNumber;
    }

}
