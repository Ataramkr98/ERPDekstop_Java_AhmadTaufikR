package com.erp.service;

import java.math.BigDecimal;
import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Service ticket (M-SVC-001). Raised from email, a web form, a portal or manually;
 * classification and priority select the SLA that applies.
 */
@Entity(name = "SvcTicket")
@Table(name = "svc_ticket")
public class SvcTicket extends BaseEntity {

    /**
     * ticketNumber
     */
    @Column(name = "ticket_number", length = 40, nullable = false)
    public String ticketNumber;

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
     * customerId
     */
    @Column(name = "customer_id")
    public Long customerId;

    /**
     * contactId
     */
    @Column(name = "contact_id")
    public Long contactId;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * serialId
     */
    @Column(name = "serial_id")
    public Long serialId;

    /**
     * Return authorisation raised from this ticket (M-SVC-006).
     */
    @Column(name = "rma_id")
    public Long rmaId;

    /**
     * serviceContractId
     */
    @Column(name = "service_contract_id")
    public Long serviceContractId;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public TicketStatus status;

    /**
     * priority
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 40)
    public TicketPriority priority;

    /**
     * channel
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 40)
    public TicketChannel channel;

    /**
     * category
     */
    @Column(name = "category", length = 60)
    public String category;

    /**
     * assignedEmployeeId
     */
    @Column(name = "assigned_employee_id")
    public Long assignedEmployeeId;

    /**
     * slaPolicyId
     */
    @Column(name = "sla_policy_id")
    public Long slaPolicyId;

    /**
     * openedAt
     */
    @Column(name = "opened_at")
    public Instant openedAt;

    /**
     * firstRespondedAt
     */
    @Column(name = "first_responded_at")
    public Instant firstRespondedAt;

    /**
     * resolvedAt
     */
    @Column(name = "resolved_at")
    public Instant resolvedAt;

    /**
     * closedAt
     */
    @Column(name = "closed_at")
    public Instant closedAt;

    /**
     * resolutionMinutes
     */
    @Column(name = "resolution_minutes", precision = 9, scale = 6)
    public BigDecimal resolutionMinutes;

    /**
     * slaBreached
     */
    @Column(name = "sla_breached")
    public Boolean slaBreached;

    /**
     * 1-5 from the post-resolution survey (M-SVC-008).
     */
    @Column(name = "csat_score", precision = 9, scale = 6)
    public BigDecimal csatScore;

    /**
     * resolutionNotes
     */
    @Column(name = "resolution_notes")
    public String resolutionNotes;

    @Override
    public String displayLabel() {
        return ticketNumber + " - " + subject;
    }

}
