package com.erp.platform;

import java.util.List;

import com.erp.config.CurrentUser;
import com.erp.shared.ApiException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Document numbering (M-ADM-009).
 *
 * <p>Every document type - sales order, journal, invoice, goods receipt - draws its
 * number from a range so that the sequence is gapless and auditable, which statutory
 * invoice numbering requires. A missing range is created on first use rather than
 * failing the transaction, so a new company works without a setup step.
 */
@ApplicationScoped
public class NumberRangeService {

    @Inject
    EntityManager em;

    @Transactional
    public String next(String documentType) {
        Long companyId = CurrentUser.companyIdOrNull();
        if (companyId == null) throw ApiException.forbidden("Sign in to an active company.");
        em.find(Company.class, companyId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        NumberRange range = find(documentType, companyId);
        if (range == null) {
            range = create(documentType, companyId);
        }

        String number = format(range);

        long current = range.nextValue == null ? 1L : range.nextValue;
        long value = current + 1;
        long max = range.maxValue == null ? 999_999_999L : range.maxValue;
        if (current > max || current == Long.MAX_VALUE) {
            throw ApiException.conflict("The number range for " + documentType
                    + " has run out. Raise the maximum in Administration.");
        }
        range.nextValue = value;
        em.merge(range);
        return number;
    }

    /** Peeks at the number the next call will issue, for the "next number" preview. */
    public String preview(String documentType) {
        NumberRange range = find(documentType, CurrentUser.companyIdOrNull());
        if (range == null) {
            range = defaultRange(documentType, CurrentUser.companyIdOrNull());
        }
        return format(range);
    }

    private NumberRange find(String documentType, Long companyId) {
        List<NumberRange> found = em.createQuery(
                "select n from NumberRange n where n.documentType = :t and n.companyId = :c",
                NumberRange.class)
                .setParameter("t", documentType)
                .setParameter("c", companyId)
                .getResultList();
        return found.isEmpty() ? null : found.get(0);
    }

    private NumberRange create(String documentType, Long companyId) {
        NumberRange range = defaultRange(documentType, companyId);
        em.persist(range);
        return range;
    }

    private NumberRange defaultRange(String documentType, Long companyId) {
        NumberRange range = new NumberRange();
        range.documentType = documentType;
        range.companyId = companyId;
        range.prefix = suggestPrefix(documentType);
        range.nextValue = 1L;
        range.minValue = 1L;
        range.maxValue = 999_999_999L;
        range.padWidth = 6;
        range.resetRule = NumberRangeReset.NEVER;
        return range;
    }

    private String format(NumberRange range) {
        long value = range.nextValue == null ? 1L : range.nextValue;
        int width = range.padWidth == null ? 0 : range.padWidth;
        String body = width > 0 ? String.format("%0" + width + "d", value) : Long.toString(value);
        String prefix = range.prefix == null ? "" : range.prefix;
        String suffix = range.suffix == null ? "" : range.suffix;
        return prefix + body + suffix;
    }

    private static String suggestPrefix(String documentType) {
        StringBuilder sb = new StringBuilder();
        for (String part : documentType.split("_")) {
            if (!part.isEmpty()) {
                sb.append(part.charAt(0));
            }
        }
        return sb.toString();
    }
}
