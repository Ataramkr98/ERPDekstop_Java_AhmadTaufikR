package com.erp.crm;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Company record with a parent/child hierarchy (M-CRM-002) so M&A structures are representable.
 * Links across to the Sales customer when one exists.
 */
@Entity(name = "CrmAccount")
@Table(name = "crm_account")
public class CrmAccount extends BaseEntity {

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
     * parentAccountId
     */
    @Column(name = "parent_account_id")
    public Long parentAccountId;

    /**
     * industry
     */
    @Column(name = "industry", length = 60)
    public String industry;

    /**
     * website
     */
    @Column(name = "website", length = 200)
    public String website;

    /**
     * phone
     */
    @Column(name = "phone", length = 40)
    public String phone;

    /**
     * annualRevenue
     */
    @Column(name = "annual_revenue", precision = 19, scale = 4)
    public BigDecimal annualRevenue;

    /**
     * employeeCount
     */
    @Column(name = "employee_count")
    public Integer employeeCount;

    /**
     * ownerEmployeeId
     */
    @Column(name = "owner_employee_id")
    public Long ownerEmployeeId;

    /**
     * SalCustomer record when this account buys from us.
     */
    @Column(name = "customer_id")
    public Long customerId;

    /**
     * billingCity
     */
    @Column(name = "billing_city", length = 100)
    public String billingCity;

    /**
     * billingCountryCode
     */
    @Column(name = "billing_country_code", length = 2)
    public String billingCountryCode;

    /**
     * 0-100 customer health, drives the post-sale view.
     */
    @Column(name = "health_score")
    public Integer healthScore;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
