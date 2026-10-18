package com.erp.hr;

/**
 * M-HR-006 pipeline. Moving a candidate to HIRED can create the employee record.
 */
public enum CandidateStage {

    APPLIED,
    SCREENED,
    INTERVIEWED,
    OFFERED,
    HIRED,
    REJECTED;

}
