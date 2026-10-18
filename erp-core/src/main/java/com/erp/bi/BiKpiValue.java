package com.erp.bi;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Computed KPI value for one period (M-BI-002). Snapshotted so dashboards render without
 * recomputing on every paint.
 */
@Entity(name = "BiKpiValue")
@Table(name = "bi_kpi_value")
public class BiKpiValue extends BaseEntity {

    /**
     * kpiId
     */
    @Column(name = "kpi_id", nullable = false)
    public Long kpiId;

    /**
     * periodYear
     */
    @Column(name = "period_year", nullable = false)
    public Integer periodYear;

    /**
     * periodMonth
     */
    @Column(name = "period_month")
    public Integer periodMonth;

    /**
     * valueDate
     */
    @Column(name = "value_date")
    public LocalDate valueDate;

    /**
     * value
     */
    @Column(name = "value", precision = 19, scale = 4)
    public BigDecimal value;

    /**
     * previousValue
     */
    @Column(name = "previous_value", precision = 19, scale = 4)
    public BigDecimal previousValue;

    /**
     * targetValue
     */
    @Column(name = "target_value", precision = 19, scale = 4)
    public BigDecimal targetValue;

    /**
     * changePercent
     */
    @Column(name = "change_percent", precision = 9, scale = 6)
    public BigDecimal changePercent;

    /**
     * GOOD, WARNING or CRITICAL against the thresholds.
     */
    @Column(name = "status", length = 20)
    public String status;

    /**
     * computedAt
     */
    @Column(name = "computed_at")
    public Instant computedAt;

}
