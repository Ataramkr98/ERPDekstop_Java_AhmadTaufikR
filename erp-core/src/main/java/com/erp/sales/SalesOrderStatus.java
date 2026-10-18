package com.erp.sales;

/**
 * M-SAL-003. Transitions are gated: an order cannot jump from DRAFT to INVOICED.
 */
public enum SalesOrderStatus {

    DRAFT,
    CONFIRMED,
    PARTIALLY_DELIVERED,
    DELIVERED,
    PARTIALLY_INVOICED,
    INVOICED,
    CLOSED,
    CANCELLED;

}
