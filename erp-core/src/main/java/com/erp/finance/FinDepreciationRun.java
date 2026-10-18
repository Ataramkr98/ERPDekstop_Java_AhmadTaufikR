package com.erp.finance;

import java.math.BigDecimal;
import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One month of depreciation for one asset. Idempotent on (asset, year, month) so a re-run
 * cannot double post.
 */
@Entity(name = "FinDepreciationRun")
@Table(name = "fin_depreciation_run")
public class FinDepreciationRun extends BaseEntity {

    /**
     * assetId
     */
    @Column(name = "asset_id", nullable = false)
    public Long assetId;

    /**
     * periodYear
     */
    @Column(name = "period_year", nullable = false)
    public Integer periodYear;

    /**
     * periodMonth
     */
    @Column(name = "period_month", nullable = false)
    public Integer periodMonth;

    /**
     * amount
     */
    @Column(name = "amount", precision = 19, scale = 4)
    public BigDecimal amount;

    /**
     * openingNetBookValue
     */
    @Column(name = "opening_net_book_value", precision = 19, scale = 4)
    public BigDecimal openingNetBookValue;

    /**
     * closingNetBookValue
     */
    @Column(name = "closing_net_book_value", precision = 19, scale = 4)
    public BigDecimal closingNetBookValue;

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

    /**
     * postedAt
     */
    @Column(name = "posted_at")
    public Instant postedAt;

}
