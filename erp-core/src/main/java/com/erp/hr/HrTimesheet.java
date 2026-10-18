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
 * Weekly timesheet header (M-HR-004, M-PJM-004). Approved hours post to project cost and feed
 * payroll overtime.
 */
@Entity(name = "HrTimesheet")
@Table(name = "hr_timesheet")
public class HrTimesheet extends BaseEntity {

    /**
     * employeeId
     */
    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    /**
     * weekStartDate
     */
    @Column(name = "week_start_date", nullable = false)
    public LocalDate weekStartDate;

    /**
     * totalHours
     */
    @Column(name = "total_hours", precision = 9, scale = 6)
    public BigDecimal totalHours;

    /**
     * billableHours
     */
    @Column(name = "billable_hours", precision = 9, scale = 6)
    public BigDecimal billableHours;

    /**
     * overtimeHours
     */
    @Column(name = "overtime_hours", precision = 9, scale = 6)
    public BigDecimal overtimeHours;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public TimesheetStatus status;

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

}
