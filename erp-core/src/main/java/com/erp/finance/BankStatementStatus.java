package com.erp.finance;

/**
 * M-FIN-005 bank reconciliation lifecycle.
 */
public enum BankStatementStatus {

    IMPORTED,
    MATCHING,
    RECONCILED,
    CLOSED;

}
