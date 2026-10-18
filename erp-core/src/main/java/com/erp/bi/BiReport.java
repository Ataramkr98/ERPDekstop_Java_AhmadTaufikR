package com.erp.bi;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Saved report definition (M-BI-003). Standard reports render in under five seconds for a
 * single company with a million rows.
 */
@Entity(name = "BiReport")
@Table(name = "bi_report")
public class BiReport extends BaseEntity {

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
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * category
     */
    @Column(name = "category", length = 60)
    public String category;

    /**
     * sourceEntity
     */
    @Column(name = "source_entity", length = 120)
    public String sourceEntity;

    /**
     * Comma-separated field list in display order.
     */
    @Column(name = "columns", length = 1000)
    public String columns;

    /**
     * filterExpression
     */
    @Column(name = "filter_expression", length = 1000)
    public String filterExpression;

    /**
     * sortExpression
     */
    @Column(name = "sort_expression", length = 300)
    public String sortExpression;

    /**
     * groupBy
     */
    @Column(name = "group_by", length = 300)
    public String groupBy;

    /**
     * Standard reports ship with the product and are read-only.
     */
    @Column(name = "system_report")
    public Boolean systemReport;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
