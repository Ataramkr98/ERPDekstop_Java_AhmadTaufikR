package com.erp.project;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Demand versus supply by role and period (M-PJM-009). Demand comes from project plans, supply
 * from HR positions; the gap is the planning signal.
 */
@Entity(name = "PjmResourcePlan")
@Table(name = "pjm_resource_plan")
public class PjmResourcePlan extends BaseEntity {

    /**
     * projectId
     */
    @Column(name = "project_id")
    public Long projectId;

    /**
     * positionId
     */
    @Column(name = "position_id")
    public Long positionId;

    /**
     * employeeId
     */
    @Column(name = "employee_id")
    public Long employeeId;

    /**
     * role
     */
    @Column(name = "role", length = 80)
    public String role;

    /**
     * periodYear
     */
    @Column(name = "period_year")
    public Integer periodYear;

    /**
     * periodMonth
     */
    @Column(name = "period_month")
    public Integer periodMonth;

    /**
     * demandHours
     */
    @Column(name = "demand_hours", precision = 9, scale = 6)
    public BigDecimal demandHours;

    /**
     * supplyHours
     */
    @Column(name = "supply_hours", precision = 9, scale = 6)
    public BigDecimal supplyHours;

    /**
     * gapHours
     */
    @Column(name = "gap_hours", precision = 9, scale = 6)
    public BigDecimal gapHours;

    /**
     * plannedCost
     */
    @Column(name = "planned_cost", precision = 19, scale = 4)
    public BigDecimal plannedCost;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
