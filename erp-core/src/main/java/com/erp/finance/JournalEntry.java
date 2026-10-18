package com.erp.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Double-entry header (M-FIN-002). The invariant sum(debits) == sum(credits) is enforced by
 * JournalService before the entry is allowed to POST.
 */
@Entity(name = "JournalEntry")
@Table(name = "journal_entry")
public class JournalEntry extends BaseEntity {

    /**
     * From NumberRange; sequential with no gaps.
     */
    @Column(name = "entry_number", length = 40)
    public String entryNumber;

    /**
     * postingDate
     */
    @Column(name = "posting_date", nullable = false)
    public LocalDate postingDate;

    /**
     * documentDate
     */
    @Column(name = "document_date")
    public LocalDate documentDate;

    /**
     * Resolved posting period; must be OPEN.
     */
    @Column(name = "period_id")
    public Long periodId;

    /**
     * SALES_INVOICE, GOODS_RECEIPT, PAYROLL, MANUAL...
     */
    @Column(name = "source_document_type", length = 60)
    public String sourceDocumentType;

    /**
     * Id of the originating operational document.
     */
    @Column(name = "source_document_id")
    public Long sourceDocumentId;

    /**
     * reference
     */
    @Column(name = "reference", length = 120)
    public String reference;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * Transaction currency.
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public JournalStatus status;

    /**
     * totalDebit
     */
    @Column(name = "total_debit", precision = 19, scale = 4)
    public BigDecimal totalDebit;

    /**
     * totalCredit
     */
    @Column(name = "total_credit", precision = 19, scale = 4)
    public BigDecimal totalCredit;

    /**
     * Set on the original when a reversing entry is posted.
     */
    @Column(name = "reversed_by_entry_id")
    public Long reversedByEntryId;

    /**
     * postedAt
     */
    @Column(name = "posted_at")
    public Instant postedAt;

    /**
     * postedBy
     */
    @Column(name = "posted_by", length = 120)
    public String postedBy;

    @Override
    public String displayLabel() {
        return entryNumber;
    }

}
