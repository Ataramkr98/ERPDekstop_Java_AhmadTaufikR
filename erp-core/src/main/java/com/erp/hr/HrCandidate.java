package com.erp.hr;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Candidate in the recruitment pipeline (M-HR-006). The offer letter is generated from a
 * template on reaching OFFERED.
 */
@Entity(name = "HrCandidate")
@Table(name = "hr_candidate")
public class HrCandidate extends BaseEntity {

    /**
     * firstName
     */
    @Column(name = "first_name", length = 120, nullable = false)
    public String firstName;

    /**
     * lastName
     */
    @Column(name = "last_name", length = 120)
    public String lastName;

    /**
     * email
     */
    @Column(name = "email", length = 160)
    public String email;

    /**
     * phone
     */
    @Column(name = "phone", length = 40)
    public String phone;

    /**
     * REFERRAL, JOB_BOARD, LINKEDIN, AGENCY, WALK_IN.
     */
    @Column(name = "source", length = 60)
    public String source;

    /**
     * requisitionId
     */
    @Column(name = "requisition_id")
    public Long requisitionId;

    /**
     * stage
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", length = 40)
    public CandidateStage stage;

    /**
     * 1-5 recruiter score.
     */
    @Column(name = "rating")
    public Integer rating;

    /**
     * appliedAt
     */
    @Column(name = "applied_at")
    public LocalDate appliedAt;

    /**
     * expectedSalary
     */
    @Column(name = "expected_salary", precision = 19, scale = 4)
    public BigDecimal expectedSalary;

    /**
     * resumeNotes
     */
    @Column(name = "resume_notes")
    public String resumeNotes;

    @Override
    public String displayLabel() {
        return firstName + " " + lastName;
    }

}
