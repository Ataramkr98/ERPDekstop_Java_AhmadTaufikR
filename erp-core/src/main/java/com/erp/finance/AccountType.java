package com.erp.finance;

/**
 * Account taxonomy from M-FIN-001. Drives which side of the trial balance an account lands on
 * and whether it closes to retained earnings at year end.
 */
public enum AccountType {

    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE;

}
