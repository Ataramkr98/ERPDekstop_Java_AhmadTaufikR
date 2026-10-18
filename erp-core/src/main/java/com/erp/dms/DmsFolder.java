package com.erp.dms;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Hierarchical folder (M-DMS-003). Folders are scoped per entity type or per project and carry
 * an access control list.
 */
@Entity(name = "DmsFolder")
@Table(name = "dms_folder")
public class DmsFolder extends BaseEntity {

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * parentId
     */
    @Column(name = "parent_id")
    public Long parentId;

    /**
     * Entity type this folder holds documents for, if scoped.
     */
    @Column(name = "entity_type", length = 120)
    public String entityType;

    /**
     * entityId
     */
    @Column(name = "entity_id")
    public Long entityId;

    /**
     * projectId
     */
    @Column(name = "project_id")
    public Long projectId;

    /**
     * PRIVATE, DEPARTMENT, COMPANY, PUBLIC.
     */
    @Column(name = "access_level", length = 20)
    public String accessLevel;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    @Override
    public String displayLabel() {
        return name;
    }

}
