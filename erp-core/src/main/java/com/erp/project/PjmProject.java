package com.erp.project;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Project master (M-PJM-001). Carries the budget, the billing method and the customer;
 * profitability is revenue recognised less cost incurred.
 */
@Entity(name = "PjmProject")
@Table(name = "pjm_project")
public class PjmProject extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 30, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * customerId
     */
    @Column(name = "customer_id")
    public Long customerId;

    /**
     * projectType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", length = 40)
    public ProjectType projectType;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public ProjectStatus status;

    /**
     * managerEmployeeId
     */
    @Column(name = "manager_employee_id")
    public Long managerEmployeeId;

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
     * startDate
     */
    @Column(name = "start_date")
    public LocalDate startDate;

    /**
     * endDate
     */
    @Column(name = "end_date")
    public LocalDate endDate;

    /**
     * actualEndDate
     */
    @Column(name = "actual_end_date")
    public LocalDate actualEndDate;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * budgetAmount
     */
    @Column(name = "budget_amount", precision = 19, scale = 4)
    public BigDecimal budgetAmount;

    /**
     * budgetHours
     */
    @Column(name = "budget_hours", precision = 19, scale = 4)
    public BigDecimal budgetHours;

    /**
     * contractValue
     */
    @Column(name = "contract_value", precision = 19, scale = 4)
    public BigDecimal contractValue;

    /**
     * plannedCost
     */
    @Column(name = "planned_cost", precision = 19, scale = 4)
    public BigDecimal plannedCost;

    /**
     * actualCost
     */
    @Column(name = "actual_cost", precision = 19, scale = 4)
    public BigDecimal actualCost;

    /**
     * actualHours
     */
    @Column(name = "actual_hours", precision = 19, scale = 4)
    public BigDecimal actualHours;

    /**
     * revenueRecognised
     */
    @Column(name = "revenue_recognised", precision = 19, scale = 4)
    public BigDecimal revenueRecognised;

    /**
     * billedAmount
     */
    @Column(name = "billed_amount", precision = 19, scale = 4)
    public BigDecimal billedAmount;

    /**
     * marginPercent
     */
    @Column(name = "margin_percent", precision = 9, scale = 6)
    public BigDecimal marginPercent;

    /**
     * completionPercent
     */
    @Column(name = "completion_percent", precision = 9, scale = 6)
    public BigDecimal completionPercent;

    /**
     * R, A or G for the status report (M-PJM-008).
     */
    @Column(name = "rag_status", length = 1)
    public String ragStatus;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
