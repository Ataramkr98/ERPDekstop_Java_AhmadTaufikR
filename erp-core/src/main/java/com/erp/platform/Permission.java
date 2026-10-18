package com.erp.platform;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Fine-grained capability such as fin.journal.post or inv.stock.adjust. Checked at method level
 * and as data predicates (NFR-S-05).
 */
@Entity(name = "Permission")
@Table(name = "permission")
public class Permission extends BaseEntity {

    /**
     * Dotted capability name.
     */
    @Column(name = "code", length = 120, nullable = false)
    public String code;

    /**
     * Division the capability belongs to.
     */
    @Column(name = "module", length = 40)
    public String module;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    @Override
    public String displayLabel() {
        return code;
    }

}
