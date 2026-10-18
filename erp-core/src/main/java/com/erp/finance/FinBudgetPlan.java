package com.erp.finance;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Budget line by fiscal year, cost centre and account (M-FIN-009). Compared against actual
 * posted balances in the Budget vs Actual report.
 */
@Entity(name = "FinBudgetPlan")
@Table(name = "fin_budget_plan")
public class FinBudgetPlan extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 40, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 200)
    public String name;

    /**
     * fiscalYear
     */
    @Column(name = "fiscal_year", nullable = false)
    public Integer fiscalYear;

    /**
     * costCenterId
     */
    @Column(name = "cost_center_id")
    public Long costCenterId;

    /**
     * accountId
     */
    @Column(name = "account_id")
    public Long accountId;

    /**
     * 1-12, or null for an annual budget.
     */
    @Column(name = "period_no")
    public Integer periodNo;

    /**
     * budgetAmount
     */
    @Column(name = "budget_amount", precision = 19, scale = 4)
    public BigDecimal budgetAmount;

    /**
     * Rolled up from posted journals.
     */
    @Column(name = "actual_amount", precision = 19, scale = 4)
    public BigDecimal actualAmount;

    /**
     * Encumbrance from approved purchase orders.
     */
    @Column(name = "committed_amount", precision = 19, scale = 4)
    public BigDecimal committedAmount;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public BudgetStatus status;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
