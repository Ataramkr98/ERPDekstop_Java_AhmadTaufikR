package com.erp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One movement on a bank statement. Auto-matched to open AR or AP items; a confirmed match
 * posts the statement line to the reconciliation account.
 */
@Entity(name = "FinBankStatementLine")
@Table(name = "fin_bank_statement_line")
public class FinBankStatementLine extends BaseEntity {

    /**
     * statementId
     */
    @Column(name = "statement_id", nullable = false)
    public Long statementId;

    /**
     * lineNo
     */
    @Column(name = "line_no", nullable = false)
    public Integer lineNo;

    /**
     * valueDate
     */
    @Column(name = "value_date")
    public LocalDate valueDate;

    /**
     * description
     */
    @Column(name = "description", length = 300)
    public String description;

    /**
     * reference
     */
    @Column(name = "reference", length = 120)
    public String reference;

    /**
     * Signed: positive is a receipt, negative a payment.
     */
    @Column(name = "amount", precision = 19, scale = 4)
    public BigDecimal amount;

    /**
     * counterparty
     */
    @Column(name = "counterparty", length = 200)
    public String counterparty;

    /**
     * matched
     */
    @Column(name = "matched")
    public Boolean matched;

    /**
     * matchedInvoiceId
     */
    @Column(name = "matched_invoice_id")
    public Long matchedInvoiceId;

    /**
     * matchedDocumentType
     */
    @Column(name = "matched_document_type", length = 40)
    public String matchedDocumentType;

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

}
