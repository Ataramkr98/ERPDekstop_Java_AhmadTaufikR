package com.erp.platform;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Key/value configuration scoped globally or per company. Every change is audit-logged
 * (M-ADM-010).
 */
@Entity(name = "SystemConfig")
@Table(name = "system_config")
public class SystemConfig extends BaseEntity {

    /**
     * configKey
     */
    @Column(name = "config_key", length = 120, nullable = false)
    public String configKey;

    /**
     * configValue
     */
    @Column(name = "config_value")
    public String configValue;

    /**
     * GLOBAL or COMPANY.
     */
    @Column(name = "scope", length = 20)
    public String scope;

    /**
     * STRING, NUMBER, BOOLEAN or JSON.
     */
    @Column(name = "value_type", length = 20)
    public String valueType;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    @Override
    public String displayLabel() {
        return configKey;
    }

}
