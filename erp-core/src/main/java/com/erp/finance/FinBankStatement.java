package com.erp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Imported bank statement (M-FIN-005). Supports CAMT.053, BAI2, MT940 and CSV.
 */
@Entity(name = "FinBankStatement")
@Table(name = "fin_bank_statement")
public class FinBankStatement extends BaseEntity {

    /**
     * bankAccountId
     */
    @Column(name = "bank_account_id", nullable = false)
    public Long bankAccountId;

    /**
     * statementRef
     */
    @Column(name = "statement_ref", length = 80)
    public String statementRef;

    /**
     * statementDate
     */
    @Column(name = "statement_date", nullable = false)
    public LocalDate statementDate;

    /**
     * fromDate
     */
    @Column(name = "from_date")
    public LocalDate fromDate;

    /**
     * toDate
     */
    @Column(name = "to_date")
    public LocalDate toDate;

    /**
     * openingBalance
     */
    @Column(name = "opening_balance", precision = 19, scale = 4)
    public BigDecimal openingBalance;

    /**
     * closingBalance
     */
    @Column(name = "closing_balance", precision = 19, scale = 4)
    public BigDecimal closingBalance;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public BankStatementStatus status;

    /**
     * lineCount
     */
    @Column(name = "line_count")
    public Integer lineCount;

    /**
     * matchedAmount
     */
    @Column(name = "matched_amount", precision = 19, scale = 4)
    public BigDecimal matchedAmount;

    /**
     * Posted once the statement is reconciled.
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

    @Override
    public String displayLabel() {
        return statementRef;
    }

}
