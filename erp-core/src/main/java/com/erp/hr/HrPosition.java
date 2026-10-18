package com.erp.hr;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Position in the org chart (M-HR-002). A position may report to two positions, which is how
 * matrix reporting is modelled.
 */
@Entity(name = "HrPosition")
@Table(name = "hr_position")
public class HrPosition extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 30, nullable = false)
    public String code;

    /**
     * title
     */
    @Column(name = "title", length = 200, nullable = false)
    public String title;

    /**
     * departmentId
     */
    @Column(name = "department_id")
    public Long departmentId;

    /**
     * reportsToPositionId
     */
    @Column(name = "reports_to_position_id")
    public Long reportsToPositionId;

    /**
     * grade
     */
    @Column(name = "grade", length = 20)
    public String grade;

    /**
     * headcount
     */
    @Column(name = "headcount")
    public Integer headcount;

    @Override
    public String displayLabel() {
        return code + " - " + title;
    }

}
