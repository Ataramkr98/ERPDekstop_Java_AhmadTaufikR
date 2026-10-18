package com.erp.finance;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Profit centre dimension (M-FIN-009) used for segment reporting alongside cost centre.
 */
@Entity(name = "FinProfitCenter")
@Table(name = "fin_profit_center")
public class FinProfitCenter extends BaseEntity {

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

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
