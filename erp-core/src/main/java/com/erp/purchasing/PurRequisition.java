package com.erp.purchasing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Purchase requisition (M-PUR-002). Created manually, from an MRP exception message, or from
 * sales-order-driven demand.
 */
@Entity(name = "PurRequisition")
@Table(name = "pur_requisition")
public class PurRequisition extends BaseEntity {

    /**
     * requisitionNumber
     */
    @Column(name = "requisition_number", length = 40, nullable = false)
    public String requisitionNumber;

    /**
     * requestedByEmployeeId
     */
    @Column(name = "requested_by_employee_id")
    public Long requestedByEmployeeId;

    /**
     * requestDate
     */
    @Column(name = "request_date", nullable = false)
    public LocalDate requestDate;

    /**
     * requiredDate
     */
    @Column(name = "required_date")
    public LocalDate requiredDate;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public PrStatus status;

    /**
     * departmentId
     */
    @Column(name = "department_id")
    public Long departmentId;

    /**
     * costCenterId
     */
    @Column(name = "cost_center_id")
    public Long costCenterId;

    /**
     * estimatedTotal
     */
    @Column(name = "estimated_total", precision = 19, scale = 4)
    public BigDecimal estimatedTotal;

    /**
     * convertedOrderId
     */
    @Column(name = "converted_order_id")
    public Long convertedOrderId;

    /**
     * justification
     */
    @Column(name = "justification")
    public String justification;

    @Override
    public String displayLabel() {
        return requisitionNumber;
    }

}
