package com.erp.purchasing;

/**
 * M-PUR-005. A tolerance breach puts the invoice ON_HOLD with a reason.
 */
public enum VendorInvoiceStatus {

    DRAFT,
    MATCHED,
    ON_HOLD,
    APPROVED,
    POSTED,
    PAID,
    VOID;

}
