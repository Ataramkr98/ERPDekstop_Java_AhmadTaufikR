package com.erp.hr;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Clock in and out record (M-HR-004). Overtime is derived against the shift rules.
 */
@Entity(name = "HrAttendance")
@Table(name = "hr_attendance")
public class HrAttendance extends BaseEntity {

    /**
     * employeeId
     */
    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    /**
     * attendanceDate
     */
    @Column(name = "attendance_date", nullable = false)
    public LocalDate attendanceDate;

    /**
     * checkIn
     */
    @Column(name = "check_in")
    public Instant checkIn;

    /**
     * checkOut
     */
    @Column(name = "check_out")
    public Instant checkOut;

    /**
     * shiftName
     */
    @Column(name = "shift_name", length = 40)
    public String shiftName;

    /**
     * hoursWorked
     */
    @Column(name = "hours_worked", precision = 9, scale = 6)
    public BigDecimal hoursWorked;

    /**
     * overtimeHours
     */
    @Column(name = "overtime_hours", precision = 9, scale = 6)
    public BigDecimal overtimeHours;

    /**
     * PRESENT, ABSENT, LATE, HALF_DAY, LEAVE.
     */
    @Column(name = "status", length = 20)
    public String status;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
