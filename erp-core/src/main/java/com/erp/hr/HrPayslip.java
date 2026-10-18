package com.erp.hr;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Gross-to-net for one employee in one period (M-HR-003). Rendered as a PDF and exported in the
 * bank file format.
 */
@Entity(name = "HrPayslip")
@Table(name = "hr_payslip")
public class HrPayslip extends BaseEntity {

    /**
     * payrollRunId
     */
    @Column(name = "payroll_run_id", nullable = false)
    public Long payrollRunId;

    /**
     * employeeId
     */
    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

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
     * basicSalary
     */
    @Column(name = "basic_salary", precision = 19, scale = 4)
    public BigDecimal basicSalary;

    /**
     * allowances
     */
    @Column(name = "allowances", precision = 19, scale = 4)
    public BigDecimal allowances;

    /**
     * overtime
     */
    @Column(name = "overtime", precision = 19, scale = 4)
    public BigDecimal overtime;

    /**
     * bonus
     */
    @Column(name = "bonus", precision = 19, scale = 4)
    public BigDecimal bonus;

    /**
     * grossPay
     */
    @Column(name = "gross_pay", precision = 19, scale = 4)
    public BigDecimal grossPay;

    /**
     * PPh 21 for Indonesia.
     */
    @Column(name = "tax_amount", precision = 19, scale = 4)
    public BigDecimal taxAmount;

    /**
     * bpjsKesehatanEmployee
     */
    @Column(name = "bpjs_kesehatan_employee", precision = 19, scale = 4)
    public BigDecimal bpjsKesehatanEmployee;

    /**
     * bpjsKesehatanEmployer
     */
    @Column(name = "bpjs_kesehatan_employer", precision = 19, scale = 4)
    public BigDecimal bpjsKesehatanEmployer;

    /**
     * bpjsJhtEmployee
     */
    @Column(name = "bpjs_jht_employee", precision = 19, scale = 4)
    public BigDecimal bpjsJhtEmployee;

    /**
     * bpjsJhtEmployer
     */
    @Column(name = "bpjs_jht_employer", precision = 19, scale = 4)
    public BigDecimal bpjsJhtEmployer;

    /**
     * bpjsJpEmployee
     */
    @Column(name = "bpjs_jp_employee", precision = 19, scale = 4)
    public BigDecimal bpjsJpEmployee;

    /**
     * bpjsJpEmployer
     */
    @Column(name = "bpjs_jp_employer", precision = 19, scale = 4)
    public BigDecimal bpjsJpEmployer;

    /**
     * otherDeductions
     */
    @Column(name = "other_deductions", precision = 19, scale = 4)
    public BigDecimal otherDeductions;

    /**
     * netPay
     */
    @Column(name = "net_pay", precision = 19, scale = 4)
    public BigDecimal netPay;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * paymentStatus
     */
    @Column(name = "payment_status", length = 20)
    public String paymentStatus;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
