package com.erp.sales;

/**
 * M-SAL-010. Commission accrues at invoice time and pays out on a monthly run.
 */
public enum CommissionStatus {

    ACCRUED,
    APPROVED,
    PAID,
    CANCELLED;

}
