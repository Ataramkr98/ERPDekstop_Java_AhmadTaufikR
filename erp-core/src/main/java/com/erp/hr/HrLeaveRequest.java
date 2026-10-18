package com.erp.hr;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Leave request and approval (M-HR-005). An approved request publishes a calendar event and
 * decrements the employee's balance.
 */
@Entity(name = "HrLeaveRequest")
@Table(name = "hr_leave_request")
public class HrLeaveRequest extends BaseEntity {

    /**
     * employeeId
     */
    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    /**
     * leaveTypeId
     */
    @Column(name = "leave_type_id", nullable = false)
    public Long leaveTypeId;

    /**
     * startDate
     */
    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    /**
     * endDate
     */
    @Column(name = "end_date", nullable = false)
    public LocalDate endDate;

    /**
     * days
     */
    @Column(name = "days", precision = 9, scale = 6, nullable = false)
    public BigDecimal days;

    /**
     * reason
     */
    @Column(name = "reason")
    public String reason;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public LeaveStatus status;

    /**
     * approvedByEmployeeId
     */
    @Column(name = "approved_by_employee_id")
    public Long approvedByEmployeeId;

    /**
     * approvedBy
     */
    @Column(name = "approved_by", length = 120)
    public String approvedBy;

    /**
     * approvedAt
     */
    @Column(name = "approved_at")
    public Instant approvedAt;

    /**
     * Required when a request is rejected (PRD flow 3).
     */
    @Column(name = "decision_comment")
    public String decisionComment;

}
