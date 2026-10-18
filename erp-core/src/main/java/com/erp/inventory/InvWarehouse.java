package com.erp.inventory;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Warehouse or site (M-INV-002). Every stock movement is warehouse-scoped.
 */
@Entity(name = "InvWarehouse")
@Table(name = "inv_warehouse")
public class InvWarehouse extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 30, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * addressLine1
     */
    @Column(name = "address_line1", length = 200)
    public String addressLine1;

    /**
     * city
     */
    @Column(name = "city", length = 100)
    public String city;

    /**
     * state
     */
    @Column(name = "state", length = 100)
    public String state;

    /**
     * countryCode
     */
    @Column(name = "country_code", length = 2)
    public String countryCode;

    /**
     * managerEmployeeId
     */
    @Column(name = "manager_employee_id")
    public Long managerEmployeeId;

    /**
     * defaultWarehouse
     */
    @Column(name = "default_warehouse")
    public Boolean defaultWarehouse;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
