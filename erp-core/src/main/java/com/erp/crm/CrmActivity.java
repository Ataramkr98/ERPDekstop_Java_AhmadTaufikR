package com.erp.crm;

import java.time.Instant;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Task, call, email or meeting logged against any record (M-CRM-005). Feeds the per-record
 * activity feed.
 */
@Entity(name = "CrmActivity")
@Table(name = "crm_activity")
public class CrmActivity extends BaseEntity {

    /**
     * activityType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 40)
    public ActivityType activityType;

    /**
     * subject
     */
    @Column(name = "subject", length = 200, nullable = false)
    public String subject;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * accountId
     */
    @Column(name = "account_id")
    public Long accountId;

    /**
     * contactId
     */
    @Column(name = "contact_id")
    public Long contactId;

    /**
     * opportunityId
     */
    @Column(name = "opportunity_id")
    public Long opportunityId;

    /**
     * leadId
     */
    @Column(name = "lead_id")
    public Long leadId;

    /**
     * ticketId
     */
    @Column(name = "ticket_id")
    public Long ticketId;

    /**
     * ownerEmployeeId
     */
    @Column(name = "owner_employee_id")
    public Long ownerEmployeeId;

    /**
     * dueDate
     */
    @Column(name = "due_date")
    public LocalDate dueDate;

    /**
     * completedAt
     */
    @Column(name = "completed_at")
    public Instant completedAt;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public ActivityStatus status;

    /**
     * durationMinutes
     */
    @Column(name = "duration_minutes")
    public Integer durationMinutes;

    @Override
    public String displayLabel() {
        return subject;
    }

}
