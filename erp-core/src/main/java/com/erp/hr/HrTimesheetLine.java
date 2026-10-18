package com.erp.hr;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Hours booked by one employee to one task on one day.
 */
@Entity(name = "HrTimesheetLine")
@Table(name = "hr_timesheet_line")
public class HrTimesheetLine extends BaseEntity {

    /**
     * timesheetId
     */
    @Column(name = "timesheet_id", nullable = false)
    public Long timesheetId;

    /**
     * workDate
     */
    @Column(name = "work_date", nullable = false)
    public LocalDate workDate;

    /**
     * hours
     */
    @Column(name = "hours", precision = 9, scale = 6)
    public BigDecimal hours;

    /**
     * projectId
     */
    @Column(name = "project_id")
    public Long projectId;

    /**
     * taskId
     */
    @Column(name = "task_id")
    public Long taskId;

    /**
     * billable
     */
    @Column(name = "billable")
    public Boolean billable;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
