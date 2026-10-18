package com.erp.inventory;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Bin location in the site / warehouse / zone / aisle / rack / bin hierarchy (M-INV-002).
 * Selectable on every movement.
 */
@Entity(name = "InvLocation")
@Table(name = "inv_location")
public class InvLocation extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 40, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 120)
    public String name;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id", nullable = false)
    public Long warehouseId;

    /**
     * parentId
     */
    @Column(name = "parent_id")
    public Long parentId;

    /**
     * locationType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", length = 40)
    public LocationType locationType;

    /**
     * aisle
     */
    @Column(name = "aisle", length = 20)
    public String aisle;

    /**
     * rack
     */
    @Column(name = "rack", length = 20)
    public String rack;

    /**
     * shelf
     */
    @Column(name = "shelf", length = 20)
    public String shelf;

    /**
     * bin
     */
    @Column(name = "bin", length = 20)
    public String bin;

    /**
     * pickable
     */
    @Column(name = "pickable")
    public Boolean pickable;

    /**
     * receivable
     */
    @Column(name = "receivable")
    public Boolean receivable;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
