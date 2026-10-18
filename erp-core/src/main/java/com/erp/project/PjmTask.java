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
 * Work breakdown structure node (M-PJM-002). Multi-level with predecessors rendered as
 * dependency arrows in the Gantt view.
 */
@Entity(name = "PjmTask")
@Table(name = "pjm_task")
public class PjmTask extends BaseEntity {

    /**
     * projectId
     */
    @Column(name = "project_id", nullable = false)
    public Long projectId;

    /**
     * parentId
     */
    @Column(name = "parent_id")
    public Long parentId;

    /**
     * Human-readable WBS path, e.g. 1.2.3.
     */
    @Column(name = "wbs_code", length = 40)
    public String wbsCode;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

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
     * actualStartDate
     */
    @Column(name = "actual_start_date")
    public LocalDate actualStartDate;

    /**
     * actualEndDate
     */
    @Column(name = "actual_end_date")
    public LocalDate actualEndDate;

    /**
     * plannedHours
     */
    @Column(name = "planned_hours", precision = 9, scale = 6)
    public BigDecimal plannedHours;

    /**
     * actualHours
     */
    @Column(name = "actual_hours", precision = 9, scale = 6)
    public BigDecimal actualHours;

    /**
     * completionPercent
     */
    @Column(name = "completion_percent", precision = 9, scale = 6)
    public BigDecimal completionPercent;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public TaskStatus status;

    /**
     * assignedEmployeeId
     */
    @Column(name = "assigned_employee_id")
    public Long assignedEmployeeId;

    /**
     * predecessorTaskId
     */
    @Column(name = "predecessor_task_id")
    public Long predecessorTaskId;

    /**
     * billable
     */
    @Column(name = "billable")
    public Boolean billable;

    /**
     * milestone
     */
    @Column(name = "milestone")
    public Boolean milestone;

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

    @Override
    public String displayLabel() {
        return wbsCode + " - " + name;
    }

}
