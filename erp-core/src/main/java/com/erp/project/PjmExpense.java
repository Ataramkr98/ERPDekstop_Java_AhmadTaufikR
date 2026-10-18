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
 * Project expense (M-PJM-005). Mark-up rules turn a cost into a billable amount.
 */
@Entity(name = "PjmExpense")
@Table(name = "pjm_expense")
public class PjmExpense extends BaseEntity {

    /**
     * projectId
     */
    @Column(name = "project_id", nullable = false)
    public Long projectId;

    /**
     * taskId
     */
    @Column(name = "task_id")
    public Long taskId;

    /**
     * employeeId
     */
    @Column(name = "employee_id")
    public Long employeeId;

    /**
     * expenseDate
     */
    @Column(name = "expense_date", nullable = false)
    public LocalDate expenseDate;

    /**
     * TRAVEL, MEALS, ACCOMMODATION, MATERIALS, OTHER.
     */
    @Column(name = "category", length = 40)
    public String category;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * amount
     */
    @Column(name = "amount", precision = 19, scale = 4)
    public BigDecimal amount;

    /**
     * markupPercent
     */
    @Column(name = "markup_percent", precision = 19, scale = 4)
    public BigDecimal markupPercent;

    /**
     * billableAmount
     */
    @Column(name = "billable_amount", precision = 19, scale = 4)
    public BigDecimal billableAmount;

    /**
     * billable
     */
    @Column(name = "billable")
    public Boolean billable;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public ExpenseStatus status;

    /**
     * Customer invoice this expense was billed on.
     */
    @Column(name = "invoice_id")
    public Long invoiceId;

    /**
     * receiptReference
     */
    @Column(name = "receipt_reference", length = 120)
    public String receiptReference;

}
