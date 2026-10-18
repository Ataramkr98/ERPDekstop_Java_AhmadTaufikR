package com.erp.bi;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Named KPI with a definition, an owner and a source query (M-BI-002). Keeps the KPI library
 * honest: every tile traces back to a defined measure.
 */
@Entity(name = "BiKpi")
@Table(name = "bi_kpi")
public class BiKpi extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 40, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * definition
     */
    @Column(name = "definition")
    public String definition;

    /**
     * FINANCE, SALES, INVENTORY, HR, PRODUCTION, SERVICE.
     */
    @Column(name = "category", length = 60)
    public String category;

    /**
     * SUM, COUNT, AVERAGE, RATIO or LAST.
     */
    @Column(name = "measure_type", length = 20)
    public String measureType;

    /**
     * Entity the measure is computed from.
     */
    @Column(name = "source_entity", length = 120)
    public String sourceEntity;

    /**
     * Field aggregated by the measure.
     */
    @Column(name = "source_field", length = 120)
    public String sourceField;

    /**
     * Optional filter in the platform filter syntax.
     */
    @Column(name = "filter_expression", length = 500)
    public String filterExpression;

    /**
     * NUMBER, CURRENCY, PERCENT, DURATION.
     */
    @Column(name = "unit", length = 20)
    public String unit;

    /**
     * targetValue
     */
    @Column(name = "target_value", precision = 19, scale = 4)
    public BigDecimal targetValue;

    /**
     * warningThreshold
     */
    @Column(name = "warning_threshold", precision = 19, scale = 4)
    public BigDecimal warningThreshold;

    /**
     * trend
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "trend", length = 40)
    public KpiTrend trend;

    /**
     * owner
     */
    @Column(name = "owner", length = 120)
    public String owner;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
