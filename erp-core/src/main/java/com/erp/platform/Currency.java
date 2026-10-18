package com.erp.platform;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * ISO 4217 currency reference data shared by every company in the installation.
 */
@Entity(name = "Currency")
@Table(name = "currency")
public class Currency extends BaseEntity {

    /**
     * ISO 4217 alphabetic code, e.g. IDR.
     */
    @Column(name = "code", length = 3, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 80, nullable = false)
    public String name;

    /**
     * symbol
     */
    @Column(name = "symbol", length = 12)
    public String symbol;

    /**
     * Decimal places; 0 for JPY, 2 for USD, 3 for BHD.
     */
    @Column(name = "minor_units")
    public Integer minorUnits;

    /**
     * enabled
     */
    @Column(name = "enabled")
    public Boolean enabled;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
