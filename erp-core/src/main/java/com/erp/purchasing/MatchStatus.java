package com.erp.purchasing;

/**
 * M-PUR-005 three-way match result between PO, goods receipt and invoice.
 */
public enum MatchStatus {

    PENDING,
    MATCHED,
    QUANTITY_VARIANCE,
    PRICE_VARIANCE,
    BOTH_VARIANCE,
    NO_MATCH;

}
