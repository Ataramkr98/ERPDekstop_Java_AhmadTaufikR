package com.erp.hr;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Employment contract (M-HR-001). History is kept per employee; only one is current.
 */
@Entity(name = "HrContract")
@Table(name = "hr_contract")
public class HrContract extends BaseEntity {

    /**
     * employeeId
     */
    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    /**
     * contractNumber
     */
    @Column(name = "contract_number", length = 60)
    public String contractNumber;

    /**
     * contractType
     */
    @Column(name = "contract_type", length = 30)
    public String contractType;

    /**
     * startDate
     */
    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    /**
     * endDate
     */
    @Column(name = "end_date")
    public LocalDate endDate;

    /**
     * probationMonths
     */
    @Column(name = "probation_months")
    public Integer probationMonths;

    /**
     * salary
     */
    @Column(name = "salary", precision = 19, scale = 4)
    public BigDecimal salary;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * current
     */
    @Column(name = "current")
    public Boolean current;

    /**
     * terms
     */
    @Column(name = "terms")
    public String terms;

    @Override
    public String displayLabel() {
        return contractNumber;
    }

}
