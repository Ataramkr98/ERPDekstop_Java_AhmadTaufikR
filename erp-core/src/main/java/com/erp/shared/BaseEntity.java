package com.erp.shared;

import java.time.Instant;

import com.erp.config.CurrentUser;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;

/**
 * Root of every business entity.
 *
 * <p>Design notes driven by the PRD:
 * <ul>
 *   <li>{@code companyId} implements multi-company (PRD M-ADM-003). Reference data such as
 *       {@code Currency} leaves it null; see {@link CrudResource#companyScoped()}.</li>
 *   <li>{@code active} is the tombstone required by NFR-DR-04 - business records are
 *       soft-deleted, only erased on legal-hold expiry or an explicit erasure request.</li>
 *   <li>{@code version} gives optimistic locking, which the conflict-flag path of the
 *       offline sync engine relies on (PRD NFR-O-03).</li>
 * </ul>
 */
@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "company_id")
    public Long companyId;

    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @Column(name = "created_by", updatable = false, length = 120)
    public String createdBy;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "updated_by", length = 120)
    public String updatedBy;

    /** NFR-DR-04: soft delete. */
    @Column(name = "active")
    public Boolean active = Boolean.TRUE;

    @Version
    @Column(name = "version")
    public Long version;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.createdBy == null) {
            this.createdBy = CurrentUser.usernameOrSystem();
        }
        if (this.updatedBy == null) {
            this.updatedBy = this.createdBy;
        }
        if (this.active == null) {
            this.active = Boolean.TRUE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = CurrentUser.usernameOrSystem();
    }

    /** Convenience for the UI: renders the entity in lookups and CSV export. */
    public String displayLabel() {
        return String.valueOf(id);
    }
}
