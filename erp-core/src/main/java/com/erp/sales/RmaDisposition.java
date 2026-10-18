package com.erp.sales;

/**
 * M-SAL-009. Drives whether a credit memo or a replacement order is generated.
 */
public enum RmaDisposition {

    REFUND,
    REPLACE,
    CREDIT,
    REPAIR,
    REJECT;

}
