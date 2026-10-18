package com.erp.finance;

/**
 * M-FIN-002 period locking. A locked period rejects any posting dated inside it.
 */
public enum PeriodStatus {

    OPEN,
    CLOSED,
    LOCKED;

}
