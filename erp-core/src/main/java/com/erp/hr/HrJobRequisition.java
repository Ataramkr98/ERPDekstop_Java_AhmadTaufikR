package com.erp.hr;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Open position request (M-HR-006). Closing it as FILLED records the time-to-hire and
 * cost-per-hire metrics.
 */
@Entity(name = "HrJobRequisition")
@Table(name = "hr_job_requisition")
public class HrJobRequisition extends BaseEntity {

    /**
     * requisitionNumber
     */
    @Column(name = "requisition_number", length = 40, nullable = false)
    public String requisitionNumber;

    /**
     * title
     */
    @Column(name = "title", length = 200, nullable = false)
    public String title;

    /**
     * departmentId
     */
    @Column(name = "department_id")
    public Long departmentId;

    /**
     * positionId
     */
    @Column(name = "position_id")
    public Long positionId;

    /**
     * positions
     */
    @Column(name = "positions")
    public Integer positions;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public RequisitionStatus status;

    /**
     * openedDate
     */
    @Column(name = "opened_date")
    public LocalDate openedDate;

    /**
     * closedDate
     */
    @Column(name = "closed_date")
    public LocalDate closedDate;

    /**
     * salaryBudget
     */
    @Column(name = "salary_budget", precision = 19, scale = 4)
    public BigDecimal salaryBudget;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    @Override
    public String displayLabel() {
        return requisitionNumber + " - " + title;
    }

}
