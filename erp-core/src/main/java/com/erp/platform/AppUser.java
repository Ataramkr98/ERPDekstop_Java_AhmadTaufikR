package com.erp.platform;

import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Login identity. Bridges to HrEmployee when the company enables self-service; otherwise it is
 * an external identity such as a vendor-portal user (PRD 9).
 */
@Entity(name = "AppUser")
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    /**
     * Login name. Unique across the installation.
     */
    @Column(name = "username", length = 80, nullable = false)
    public String username;

    /**
     * fullName
     */
    @Column(name = "full_name", length = 200)
    public String fullName;

    /**
     * email
     */
    @Column(name = "email", length = 160)
    public String email;

    /**
     * PBKDF2WithHmacSHA256 hash, never stored in plain text (NFR-S-02).
     */
    @Column(name = "password_hash", length = 255)
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String passwordHash;

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    public String password;

    /**
     * Per-user UI locale (M-ADM-005).
     */
    @Column(name = "locale", length = 16)
    public String locale;

    /**
     * Company selected at login; the JWT carries it as the tenancy claim.
     */
    @Column(name = "default_company_id")
    public Long defaultCompanyId;

    /**
     * Link to HrEmployee when the user is an employee.
     */
    @Column(name = "employee_id")
    public Long employeeId;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public UserStatus status;

    /**
     * Reset on success; lockout triggers at the configured threshold.
     */
    @Column(name = "failed_login_count")
    public Integer failedLoginCount;

    /**
     * End of the lockout window.
     */
    @Column(name = "locked_until")
    public Instant lockedUntil;

    /**
     * lastLoginAt
     */
    @Column(name = "last_login_at")
    public Instant lastLoginAt;

    /**
     * mustChangePassword
     */
    @Column(name = "must_change_password")
    public Boolean mustChangePassword;

    @Override
    public String displayLabel() {
        return username + " - " + fullName;
    }

}
