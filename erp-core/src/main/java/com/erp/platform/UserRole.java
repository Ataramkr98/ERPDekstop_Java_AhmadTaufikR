package com.erp.platform;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Grants one role to one user (M-ADM-002). A user accumulates the union of the permissions of
 * every role they hold.
 */
@Entity(name = "UserRole")
@Table(name = "user_role")
public class UserRole extends BaseEntity {

    /**
     * userId
     */
    @Column(name = "user_id", nullable = false)
    public Long userId;

    /**
     * roleCode
     */
    @Column(name = "role_code", length = 40, nullable = false)
    public String roleCode;

    @Override
    public String displayLabel() {
        return roleCode;
    }

}
