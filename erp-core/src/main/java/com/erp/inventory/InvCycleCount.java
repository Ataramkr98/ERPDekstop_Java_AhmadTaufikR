package com.erp.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Cycle count header (M-INV-008). Scheduled by ABC class; affected items are frozen for the
 * duration of the count and variance above tolerance needs approval.
 */
@Entity(name = "InvCycleCount")
@Table(name = "inv_cycle_count")
public class InvCycleCount extends BaseEntity {

    /**
     * countNumber
     */
    @Column(name = "count_number", length = 40, nullable = false)
    public String countNumber;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id", nullable = false)
    public Long warehouseId;

    /**
     * countDate
     */
    @Column(name = "count_date", nullable = false)
    public LocalDate countDate;

    /**
     * countType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "count_type", length = 40)
    public CountType countType;

    /**
     * abcClass
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "abc_class", length = 40)
    public AbcClass abcClass;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public CycleCountStatus status;

    /**
     * Hides the system quantity from the counter.
     */
    @Column(name = "blind_count")
    public Boolean blindCount;

    /**
     * frozen
     */
    @Column(name = "frozen")
    public Boolean frozen;

    /**
     * lineCount
     */
    @Column(name = "line_count")
    public Integer lineCount;

    /**
     * varianceCount
     */
    @Column(name = "variance_count")
    public Integer varianceCount;

    /**
     * varianceValue
     */
    @Column(name = "variance_value", precision = 19, scale = 4)
    public BigDecimal varianceValue;

    /**
     * varianceTolerancePercent
     */
    @Column(name = "variance_tolerance_percent", precision = 9, scale = 6)
    public BigDecimal varianceTolerancePercent;

    /**
     * countedBy
     */
    @Column(name = "counted_by", length = 120)
    public String countedBy;

    /**
     * approvedBy
     */
    @Column(name = "approved_by", length = 120)
    public String approvedBy;

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

    @Override
    public String displayLabel() {
        return countNumber;
    }

}
