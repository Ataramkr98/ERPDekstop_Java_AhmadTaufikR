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
 * Billing milestone (M-PJM-006). Completion raises an invoice draft for the milestone amount on
 * a fixed-price project.
 */
@Entity(name = "PjmMilestone")
@Table(name = "pjm_milestone")
public class PjmMilestone extends BaseEntity {

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
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * sequence
     */
    @Column(name = "sequence", length = 20)
    public String sequence;

    /**
     * plannedDate
     */
    @Column(name = "planned_date")
    public LocalDate plannedDate;

    /**
     * achievedDate
     */
    @Column(name = "achieved_date")
    public LocalDate achievedDate;

    /**
     * milestoneAmount
     */
    @Column(name = "milestone_amount", precision = 19, scale = 4)
    public BigDecimal milestoneAmount;

    /**
     * percentOfContract
     */
    @Column(name = "percent_of_contract", precision = 9, scale = 6)
    public BigDecimal percentOfContract;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public MilestoneStatus status;

    /**
     * invoiceId
     */
    @Column(name = "invoice_id")
    public Long invoiceId;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return name;
    }

}
