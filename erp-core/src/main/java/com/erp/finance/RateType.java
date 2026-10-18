package com.erp.finance;

/**
 * M-FIN-007. Period-end revaluation uses CLOSING; P&L translation uses AVERAGE.
 */
public enum RateType {

    SPOT,
    AVERAGE,
    CLOSING,
    BUDGET;

}
