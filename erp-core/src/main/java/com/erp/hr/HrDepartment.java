package com.erp.hr;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Organisational unit (M-HR-002). Built into the org chart through positions so that matrix
 * reporting is representable.
 */
@Entity(name = "HrDepartment")
@Table(name = "hr_department")
public class HrDepartment extends BaseEntity {

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
     * costCenterId
     */
    @Column(name = "cost_center_id")
    public Long costCenterId;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
