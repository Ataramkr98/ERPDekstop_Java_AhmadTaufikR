package com.erp.hr;

/**
 * A posted payroll run is frozen and its journal cannot be re-generated.
 */
public enum PayrollStatus {

    DRAFT,
    PREVIEWED,
    APPROVED,
    POSTED,
    CANCELLED;

}
