package com.erp.hr;

import java.math.BigDecimal;
import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * One payroll period (M-HR-003). Posting creates a single balanced journal: salary expense and
 * employer contributions against payroll payable and tax payable.
 */
@Entity(name = "HrPayrollRun")
@Table(name = "hr_payroll_run")
public class HrPayrollRun extends BaseEntity {

    /**
     * periodYear
     */
    @Column(name = "period_year", nullable = false)
    public Integer periodYear;

    /**
     * periodMonth
     */
    @Column(name = "period_month", nullable = false)
    public Integer periodMonth;

    /**
     * e.g. 2026-08, used as the display key.
     */
    @Column(name = "period_label", length = 20)
    public String periodLabel;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public PayrollStatus status;

    /**
     * employeeCount
     */
    @Column(name = "employee_count")
    public Integer employeeCount;

    /**
     * totalGross
     */
    @Column(name = "total_gross", precision = 19, scale = 4)
    public BigDecimal totalGross;

    /**
     * totalAllowance
     */
    @Column(name = "total_allowance", precision = 19, scale = 4)
    public BigDecimal totalAllowance;

    /**
     * totalOvertime
     */
    @Column(name = "total_overtime", precision = 19, scale = 4)
    public BigDecimal totalOvertime;

    /**
     * totalTax
     */
    @Column(name = "total_tax", precision = 19, scale = 4)
    public BigDecimal totalTax;

    /**
     * totalBpjsEmployee
     */
    @Column(name = "total_bpjs_employee", precision = 19, scale = 4)
    public BigDecimal totalBpjsEmployee;

    /**
     * totalBpjsEmployer
     */
    @Column(name = "total_bpjs_employer", precision = 19, scale = 4)
    public BigDecimal totalBpjsEmployer;

    /**
     * totalDeduction
     */
    @Column(name = "total_deduction", precision = 19, scale = 4)
    public BigDecimal totalDeduction;

    /**
     * totalNet
     */
    @Column(name = "total_net", precision = 19, scale = 4)
    public BigDecimal totalNet;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

    /**
     * postedAt
     */
    @Column(name = "posted_at")
    public Instant postedAt;

    @Override
    public String displayLabel() {
        return periodLabel;
    }

}
