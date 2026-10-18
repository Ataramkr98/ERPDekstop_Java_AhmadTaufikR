package com.erp.logistics;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Carrier master with rate cards and transit times (M-LOG-003).
 */
@Entity(name = "LogCarrier")
@Table(name = "log_carrier")
public class LogCarrier extends BaseEntity {

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
     * contactPerson
     */
    @Column(name = "contact_person", length = 160)
    public String contactPerson;

    /**
     * email
     */
    @Column(name = "email", length = 160)
    public String email;

    /**
     * phone
     */
    @Column(name = "phone", length = 40)
    public String phone;

    /**
     * ECONOMY, STANDARD, EXPRESS, SAME_DAY.
     */
    @Column(name = "service_level", length = 40)
    public String serviceLevel;

    /**
     * Contains {tracking} substituted at render time.
     */
    @Column(name = "tracking_url_template", length = 300)
    public String trackingUrlTemplate;

    /**
     * Purchasing vendor used to pay this carrier's invoices.
     */
    @Column(name = "vendor_id")
    public Long vendorId;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
