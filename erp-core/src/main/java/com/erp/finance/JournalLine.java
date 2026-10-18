package com.erp.finance;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One side of a journal entry. Debit and credit are mutually exclusive; the line carries the
 * cost and profit centre dimensions used for allocation (M-FIN-009).
 */
@Entity(name = "JournalLine")
@Table(name = "journal_line")
public class JournalLine extends BaseEntity {

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id", nullable = false)
    public Long journalEntryId;

    /**
     * lineNo
     */
    @Column(name = "line_no", nullable = false)
    public Integer lineNo;

    /**
     * accountId
     */
    @Column(name = "account_id", nullable = false)
    public Long accountId;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * debit
     */
    @Column(name = "debit", precision = 19, scale = 4)
    public BigDecimal debit;

    /**
     * credit
     */
    @Column(name = "credit", precision = 19, scale = 4)
    public BigDecimal credit;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * Amount in transaction currency for multi-currency books.
     */
    @Column(name = "currency_debit", precision = 19, scale = 4)
    public BigDecimal currencyDebit;

    /**
     * currencyCredit
     */
    @Column(name = "currency_credit", precision = 19, scale = 4)
    public BigDecimal currencyCredit;

    /**
     * Rate applied to convert to the company base currency.
     */
    @Column(name = "exchange_rate", precision = 9, scale = 6)
    public BigDecimal exchangeRate;

    /**
     * costCenterId
     */
    @Column(name = "cost_center_id")
    public Long costCenterId;

    /**
     * profitCenterId
     */
    @Column(name = "profit_center_id")
    public Long profitCenterId;

    /**
     * currencyId
     */
    @Column(name = "currency_id")
    public Long currencyId;

    /**
     * taxCode
     */
    @Column(name = "tax_code", length = 40)
    public String taxCode;

}
