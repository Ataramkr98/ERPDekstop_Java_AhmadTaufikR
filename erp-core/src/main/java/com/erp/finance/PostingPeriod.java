package com.erp.finance;

import java.time.Instant;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Fiscal period (M-FIN-002). Every journal is dated into a period and a CLOSED or LOCKED period
 * refuses new postings, which is what makes the books defensible.
 */
@Entity(name = "PostingPeriod")
@Table(name = "posting_period")
public class PostingPeriod extends BaseEntity {

    /**
     * fiscalYear
     */
    @Column(name = "fiscal_year", nullable = false)
    public Integer fiscalYear;

    /**
     * 1-12, or 13-15 for adjustment periods.
     */
    @Column(name = "period_no", nullable = false)
    public Integer periodNo;

    /**
     * name
     */
    @Column(name = "name", length = 60)
    public String name;

    /**
     * startDate
     */
    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    /**
     * endDate
     */
    @Column(name = "end_date", nullable = false)
    public LocalDate endDate;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public PeriodStatus status;

    /**
     * closedAt
     */
    @Column(name = "closed_at")
    public Instant closedAt;

    /**
     * closedBy
     */
    @Column(name = "closed_by", length = 120)
    public String closedBy;

    @Override
    public String displayLabel() {
        return name;
    }

}
