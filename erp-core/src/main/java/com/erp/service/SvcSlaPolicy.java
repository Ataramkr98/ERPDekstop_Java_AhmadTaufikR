package com.erp.service;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * SLA policy (M-SVC-002). One policy per priority with a first-response and a resolution
 * target; a breach triggers escalation.
 */
@Entity(name = "SvcSlaPolicy")
@Table(name = "svc_sla_policy")
public class SvcSlaPolicy extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 30, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 120, nullable = false)
    public String name;

    /**
     * priority
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 40)
    public TicketPriority priority;

    /**
     * targetType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 40)
    public SlaTargetType targetType;

    /**
     * firstResponseMinutes
     */
    @Column(name = "first_response_minutes")
    public Integer firstResponseMinutes;

    /**
     * resolutionMinutes
     */
    @Column(name = "resolution_minutes")
    public Integer resolutionMinutes;

    /**
     * escalationMinutes
     */
    @Column(name = "escalation_minutes")
    public Integer escalationMinutes;

    /**
     * escalateToEmployeeId
     */
    @Column(name = "escalate_to_employee_id")
    public Long escalateToEmployeeId;

    /**
     * businessHoursOnly
     */
    @Column(name = "business_hours_only")
    public Boolean businessHoursOnly;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
