package com.erp.platform;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Document numbering per company and document type with an optional prefix and reset rule
 * (M-ADM-009). Gaps are tracked because statutory sequences forbid them.
 */
@Entity(name = "NumberRange")
@Table(name = "number_range")
public class NumberRange extends BaseEntity {

    /**
     * e.g. SALES_ORDER, JOURNAL, CUSTOMER_INVOICE.
     */
    @Column(name = "document_type", length = 60, nullable = false)
    public String documentType;

    /**
     * prefix
     */
    @Column(name = "prefix", length = 20)
    public String prefix;

    /**
     * suffix
     */
    @Column(name = "suffix", length = 20)
    public String suffix;

    /**
     * nextValue
     */
    @Column(name = "next_value", nullable = false)
    public Long nextValue;

    /**
     * minValue
     */
    @Column(name = "min_value")
    public Long minValue;

    /**
     * maxValue
     */
    @Column(name = "max_value")
    public Long maxValue;

    /**
     * Zero-pad width, so 42 becomes 000042.
     */
    @Column(name = "pad_width")
    public Integer padWidth;

    /**
     * resetRule
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reset_rule", length = 40)
    public NumberRangeReset resetRule;

    /**
     * Year the counter was last reset, for YEARLY ranges.
     */
    @Column(name = "last_reset_year")
    public Integer lastResetYear;

    @Override
    public String displayLabel() {
        return documentType;
    }

}
