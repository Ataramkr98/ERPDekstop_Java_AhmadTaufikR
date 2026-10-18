package com.erp.project;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Periodic status report (M-PJM-008) generated from a template against live project data with a
 * RAG rating per WBS node.
 */
@Entity(name = "PjmStatusReport")
@Table(name = "pjm_status_report")
public class PjmStatusReport extends BaseEntity {

    /**
     * projectId
     */
    @Column(name = "project_id", nullable = false)
    public Long projectId;

    /**
     * reportDate
     */
    @Column(name = "report_date", nullable = false)
    public LocalDate reportDate;

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
     * ragStatus
     */
    @Column(name = "rag_status", length = 1)
    public String ragStatus;

    /**
     * completionPercent
     */
    @Column(name = "completion_percent", precision = 9, scale = 6)
    public BigDecimal completionPercent;

    /**
     * costToDate
     */
    @Column(name = "cost_to_date", precision = 19, scale = 4)
    public BigDecimal costToDate;

    /**
     * revenueToDate
     */
    @Column(name = "revenue_to_date", precision = 19, scale = 4)
    public BigDecimal revenueToDate;

    /**
     * accomplishments
     */
    @Column(name = "accomplishments")
    public String accomplishments;

    /**
     * nextSteps
     */
    @Column(name = "next_steps")
    public String nextSteps;

    /**
     * risksAndIssues
     */
    @Column(name = "risks_and_issues")
    public String risksAndIssues;

    /**
     * reportedBy
     */
    @Column(name = "reported_by", length = 120)
    public String reportedBy;

}
