package com.erp.finance;

/**
 * A posted journal is immutable; correcting it posts a reversing entry.
 */
public enum JournalStatus {

    DRAFT,
    POSTED,
    REVERSED;

}
