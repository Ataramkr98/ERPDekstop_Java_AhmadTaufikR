package com.erp.platform;

import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Append-only trail of every create, update and delete (X-AUD-001, M-ADM-006, NFR-S-06).
 * Queried by user, entity and date range; never updated in place.
 */
@Entity(name = "AuditLog")
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    /**
     * CREATE, UPDATE, DELETE or a custom business action.
     */
    @Column(name = "action", length = 20, nullable = false)
    public String action;

    /**
     * entityType
     */
    @Column(name = "entity_type", length = 120)
    public String entityType;

    /**
     * entityId
     */
    @Column(name = "entity_id")
    public Long entityId;

    /**
     * JSON snapshot of the record before the change.
     */
    @Column(name = "before_value")
    public String beforeValue;

    /**
     * JSON snapshot of the record after the change.
     */
    @Column(name = "after_value")
    public String afterValue;

    /**
     * username
     */
    @Column(name = "username", length = 120)
    public String username;

    /**
     * occurredAt
     */
    @Column(name = "occurred_at")
    public Instant occurredAt;

    /**
     * sourceIp
     */
    @Column(name = "source_ip", length = 64)
    public String sourceIp;

}
