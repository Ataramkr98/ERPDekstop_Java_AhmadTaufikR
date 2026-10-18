package com.erp.sales;

import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Price list header (M-SAL-004). The pricing engine walks a priority chain to pick the
 * applicable list for a given customer, item, quantity and date.
 */
@Entity(name = "SalPriceList")
@Table(name = "sal_price_list")
public class SalPriceList extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 30, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 120, nullable = false)
    public String name;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * validFrom
     */
    @Column(name = "valid_from")
    public LocalDate validFrom;

    /**
     * validTo
     */
    @Column(name = "valid_to")
    public LocalDate validTo;

    /**
     * defaultList
     */
    @Column(name = "default_list")
    public Boolean defaultList;

    /**
     * Lower wins when several lists apply.
     */
    @Column(name = "priority")
    public Integer priority;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
