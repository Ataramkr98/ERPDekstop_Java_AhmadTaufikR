package com.erp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.erp.config.CurrentUser;
import com.erp.platform.NumberRangeService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.erp.shared.ApiException;

/**
 * The general ledger posting engine.
 *
 * <p>Per docs/ARCHITECTURE.md section 5 the GL is the single source of truth: every
 * operational event with a financial effect posts a balanced journal through this
 * service. Three invariants are enforced here rather than in each caller:
 *
 * <ol>
 *   <li>the entry must have at least two lines;</li>
 *   <li>sum of debits must equal sum of credits;</li>
 *   <li>the posting date must fall in an open period.</li>
 * </ol>
 *
 * <p>Callers therefore cannot create an unbalanced book even by accident, and period
 * locking is a single check rather than something each module has to remember.
 */
@ApplicationScoped
public class JournalService {

    /** One side of a journal, as supplied by a calling module. */
    public static class Line {
        public Long accountId;
        public BigDecimal debit = BigDecimal.ZERO;
        public BigDecimal credit = BigDecimal.ZERO;
        public String description;
        public Long costCenterId;
        public Long profitCenterId;

        public Line() {
        }

        public Line(Long accountId, BigDecimal debit, BigDecimal credit, String description) {
            this.accountId = accountId;
            this.debit = debit == null ? BigDecimal.ZERO : debit;
            this.credit = credit == null ? BigDecimal.ZERO : credit;
            this.description = description;
        }
    }

    @Inject
    EntityManager em;
    @Inject com.erp.shared.BusinessRecords records;
    @Inject com.erp.platform.AuditService audit;

    @Inject
    NumberRangeService numberRange;

    /**
     * Builds and posts a balanced journal.
     *
     * @return the posted entry, with its generated entry number
     */
    @Transactional
    public JournalEntry post(String sourceDocumentType, Long sourceDocumentId,
                             LocalDate postingDate, String description, List<Line> lines) {
        return post(sourceDocumentType, sourceDocumentId, postingDate, description, lines, null);
    }

    @Transactional
    public JournalEntry post(String sourceDocumentType, Long sourceDocumentId,
                             LocalDate postingDate, String description, List<Line> lines,
                             String currencyCode) {
        if (lines == null || lines.size() < 2) {
            throw ApiException.badRequest("A journal entry needs at least two lines.");
        }

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (Line line : lines) {
            if (line == null) throw ApiException.badRequest("A journal line is missing.");
            if (line.accountId == null) {
                throw ApiException.badRequest("Every journal line needs an account.");
            }
            FinAccount account = records.lock(FinAccount.class, line.accountId);
            if (Boolean.TRUE.equals(account.isGroup)) throw ApiException.badRequest("Post to a detail account, not an account group.");
            BigDecimal debit = nz(line.debit);
            BigDecimal credit = nz(line.credit);
            if (debit.signum() != 0 && credit.signum() != 0) {
                throw ApiException.badRequest(
                        "A journal line cannot have both a debit and a credit. Split it into two lines.");
            }
            if (debit.signum() < 0 || credit.signum() < 0) {
                throw ApiException.badRequest("Journal amounts cannot be negative. Use the opposite side instead.");
            }
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw ApiException.badRequest("The entry is out of balance: debits total "
                    + totalDebit.toPlainString() + " but credits total "
                    + totalCredit.toPlainString() + ".");
        }
        if (totalDebit.signum() == 0) {
            throw ApiException.badRequest("The entry has no value. Nothing was posted.");
        }

        LocalDate date = postingDate == null ? LocalDate.now() : postingDate;
        PostingPeriod period = openPeriod(date);

        JournalEntry entry = new JournalEntry();
        entry.entryNumber = numberRange.next("JOURNAL");
        entry.postingDate = date;
        entry.documentDate = date;
        entry.periodId = period.id;
        entry.sourceDocumentType = sourceDocumentType;
        entry.sourceDocumentId = sourceDocumentId;
        entry.description = description;
        entry.currency = currencyCode;
        entry.status = JournalStatus.DRAFT;
        entry.totalDebit = totalDebit;
        entry.totalCredit = totalCredit;
        entry.companyId = CurrentUser.companyIdOrNull();
        em.persist(entry);

        int lineNo = 10;
        for (Line line : lines) {
            JournalLine journalLine = new JournalLine();
            journalLine.journalEntryId = entry.id;
            journalLine.lineNo = lineNo;
            journalLine.accountId = line.accountId;
            journalLine.description = line.description;
            journalLine.debit = nz(line.debit);
            journalLine.credit = nz(line.credit);
            journalLine.currency = currencyCode;
            journalLine.costCenterId = line.costCenterId;
            journalLine.profitCenterId = line.profitCenterId;
            journalLine.companyId = entry.companyId;
            em.persist(journalLine);
            lineNo += 10;
        }

        entry.status = JournalStatus.POSTED;
        entry.postedAt = java.time.Instant.now();
        entry.postedBy = CurrentUser.usernameOrSystem();
        em.merge(entry);
        audit.custom("POST", "JournalEntry", entry.id, "Posted " + entry.entryNumber);
        return entry;
    }

    /** Posts the mirror image of an entry and cross-links the two (M-FIN-002). */
    @Transactional
    public JournalEntry reverse(Long entryId, String reason) {
        JournalEntry original = records.lock(JournalEntry.class, entryId);
        if (original == null) {
            throw ApiException.notFound("Journal entry " + entryId + " was not found.");
        }
        if (original.status != JournalStatus.POSTED || original.reversedByEntryId != null) {
            throw ApiException.badRequest("Only a posted entry can be reversed.");
        }
        if ("JOURNAL_REVERSAL".equals(original.sourceDocumentType))
            throw ApiException.badRequest("A reversal cannot itself be reversed. Create a new source document instead.");
        if ("GOODS_RECEIPT".equals(original.sourceDocumentType) || "SALES_DELIVERY".equals(original.sourceDocumentType))
            throw ApiException.badRequest("This journal records a stock movement. Correct the stock document before reversing its accounting.");
        com.erp.sales.SalInvoice customerInvoice = null;
        com.erp.purchasing.PurInvoice vendorInvoice = null;
        if ("CUSTOMER_INVOICE".equals(original.sourceDocumentType)) {
            customerInvoice = records.lock(com.erp.sales.SalInvoice.class, original.sourceDocumentId);
            if (nz(customerInvoice.paidAmount).signum() != 0) throw ApiException.badRequest("An invoice with payments cannot be reversed directly.");
        } else if ("VENDOR_INVOICE".equals(original.sourceDocumentType)) {
            vendorInvoice = records.lock(com.erp.purchasing.PurInvoice.class, original.sourceDocumentId);
            if (nz(vendorInvoice.paidAmount).signum() != 0) throw ApiException.badRequest("An invoice with payments cannot be reversed directly.");
        }

        List<JournalLine> existing = em.createQuery(
                "select l from JournalLine l where l.journalEntryId = :id order by l.lineNo",
                JournalLine.class)
                .setParameter("id", entryId)
                .getResultList();

        List<Line> reversed = new ArrayList<>();
        for (JournalLine line : existing) {
            Line reversedLine = new Line(line.accountId, nz(line.credit), nz(line.debit),
                    "Reversal: " + (reason == null ? original.entryNumber : reason));
            reversedLine.costCenterId = line.costCenterId;
            reversedLine.profitCenterId = line.profitCenterId;
            reversed.add(reversedLine);
        }

        JournalEntry reversal = post("JOURNAL_REVERSAL", original.id,
                LocalDate.now(), "Reversal of " + original.entryNumber, reversed, original.currency);
        original.reversedByEntryId = reversal.id;
        original.status = JournalStatus.REVERSED;
        if (customerInvoice != null) {
            var customer = records.lock(com.erp.sales.SalCustomer.class, customerInvoice.customerId);
            customer.currentBalance = nz(customer.currentBalance).subtract(nz(customerInvoice.balanceAmount));
            customerInvoice.balanceAmount = BigDecimal.ZERO;
            customerInvoice.status = com.erp.sales.InvoiceStatus.VOID;
            audit.custom("VOID", "SalInvoice", customerInvoice.id, "Voided by journal reversal " + reversal.entryNumber);
        }
        if (vendorInvoice != null) {
            vendorInvoice.balanceAmount = BigDecimal.ZERO;
            vendorInvoice.status = com.erp.purchasing.VendorInvoiceStatus.VOID;
            audit.custom("VOID", "PurInvoice", vendorInvoice.id, "Voided by journal reversal " + reversal.entryNumber);
        }
        audit.custom("REVERSE", "JournalEntry", original.id, "Reversed by " + reversal.entryNumber);
        em.merge(original);
        return reversal;
    }

    /**
     * Resolves a GL account by the code used in the seeded chart of accounts.
     * Fails with an actionable message rather than null, because a missing control
     * account is a setup problem the administrator has to fix.
     */
    public Long accountIdByCode(String code) {
        Long companyId = CurrentUser.companyIdOrNull();
        List<FinAccount> found = em.createQuery(
                "select a from FinAccount a where a.active = true and a.code = :code and a.companyId = :c",
                FinAccount.class)
                .setParameter("code", code)
                .setParameter("c", companyId)
                .getResultList();
        if (found.isEmpty()) {
            throw ApiException.badRequest(
                    "Account " + code + " is missing from the chart of accounts. Add it in Finance "
                            + "before posting this transaction.");
        }
        return found.get(0).id;
    }

    /** Returns the open period for a date, creating the period if the calendar is empty. */
    @Transactional(Transactional.TxType.MANDATORY)
    public PostingPeriod openPeriod(LocalDate date) {
        Long companyId = CurrentUser.companyIdOrNull();
        records.lock(com.erp.platform.Company.class, companyId);
        List<PostingPeriod> periods = em.createQuery(
                "select p from PostingPeriod p where p.companyId = :c and p.startDate <= :d and p.endDate >= :d",
                PostingPeriod.class)
                .setParameter("c", companyId)
                .setParameter("d", date)
                .getResultList();

        if (periods.isEmpty()) {
            // A company with no calendar would otherwise be unable to post anything at
            // all, so the period is created on demand rather than blocking the user.
            PostingPeriod created = new PostingPeriod();
            created.companyId = companyId;
            created.fiscalYear = date.getYear();
            created.periodNo = date.getMonthValue();
            created.name = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            created.startDate = date.withDayOfMonth(1);
            created.endDate = date.withDayOfMonth(date.lengthOfMonth());
            created.status = PeriodStatus.OPEN;
            em.persist(created);
            return created;
        }

        PostingPeriod period = periods.get(0);
        if (period.status != PeriodStatus.OPEN) {
            throw ApiException.badRequest("Period " + period.name + " is "
                    + period.status.name().toLowerCase()
                    + ". Reopen it in Finance before posting to " + date + ".");
        }
        return period;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
