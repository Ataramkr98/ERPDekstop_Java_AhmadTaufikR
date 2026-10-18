package com.erp.purchasing;

/**
 * M-PUR-003. An order below the approval threshold skips PENDING_APPROVAL.
 */
public enum PoStatus {

    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    RELEASED,
    PARTIALLY_RECEIVED,
    RECEIVED,
    PARTIALLY_INVOICED,
    INVOICED,
    CLOSED,
    CANCELLED;

}
