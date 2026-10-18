package com.erp.bi;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Role-based dashboard (M-BI-001, M-BI-008). Each role has a default dashboard and users can
 * pin additional ones.
 */
@Entity(name = "BiDashboard")
@Table(name = "bi_dashboard")
public class BiDashboard extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 40, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * Role this dashboard is the default for.
     */
    @Column(name = "role_code", length = 40)
    public String roleCode;

    /**
     * defaultDashboard
     */
    @Column(name = "default_dashboard")
    public Boolean defaultDashboard;

    /**
     * sortOrder
     */
    @Column(name = "sort_order")
    public Integer sortOrder;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
