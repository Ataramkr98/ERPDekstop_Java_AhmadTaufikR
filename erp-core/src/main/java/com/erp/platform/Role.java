package com.erp.platform;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Named, data-driven set of permissions. Deny overrides allow (M-ADM-002).
 */
@Entity(name = "Role")
@Table(name = "role")
public class Role extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 40, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 120, nullable = false)
    public String name;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * System roles cannot be deleted by an administrator.
     */
    @Column(name = "system_role")
    public Boolean systemRole;

    /**
     * Comma-separated permission codes granted by this role.
     */
    @Column(name = "permissions")
    public String permissions;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
