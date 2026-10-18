package com.erp.finance;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Cost centre (M-FIN-009). Allocation cycles spread one centre's expenses across N targets by
 * fixed percentage or a variable driver.
 */
@Entity(name = "FinCostCenter")
@Table(name = "fin_cost_center")
public class FinCostCenter extends BaseEntity {

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
     * parentId
     */
    @Column(name = "parent_id")
    public Long parentId;

    /**
     * managerEmployeeId
     */
    @Column(name = "manager_employee_id")
    public Long managerEmployeeId;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
