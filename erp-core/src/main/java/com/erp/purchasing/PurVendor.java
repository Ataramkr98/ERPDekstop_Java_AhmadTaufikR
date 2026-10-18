package com.erp.purchasing;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Vendor master (M-PUR-001). Holds bank details for the payment run and a tax id validated
 * against a configurable algorithm.
 */
@Entity(name = "PurVendor")
@Table(name = "pur_vendor")
public class PurVendor extends BaseEntity {

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
     * vendorType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_type", length = 40)
    public VendorType vendorType;

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
     * website
     */
    @Column(name = "website", length = 200)
    public String website;

    /**
     * taxId
     */
    @Column(name = "tax_id", length = 40)
    public String taxId;

    /**
     * addressLine1
     */
    @Column(name = "address_line1", length = 200)
    public String addressLine1;

    /**
     * addressLine2
     */
    @Column(name = "address_line2", length = 200)
    public String addressLine2;

    /**
     * city
     */
    @Column(name = "city", length = 100)
    public String city;

    /**
     * state
     */
    @Column(name = "state", length = 100)
    public String state;

    /**
     * postalCode
     */
    @Column(name = "postal_code", length = 20)
    public String postalCode;

    /**
     * countryCode
     */
    @Column(name = "country_code", length = 2)
    public String countryCode;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * paymentTermDays
     */
    @Column(name = "payment_term_days")
    public Integer paymentTermDays;

    /**
     * bankName
     */
    @Column(name = "bank_name", length = 120)
    public String bankName;

    /**
     * bankAccountNumber
     */
    @Column(name = "bank_account_number", length = 60)
    public String bankAccountNumber;

    /**
     * bankAccountHolder
     */
    @Column(name = "bank_account_holder", length = 160)
    public String bankAccountHolder;

    /**
     * swift
     */
    @Column(name = "swift", length = 20)
    public String swift;

    /**
     * payableAccountId
     */
    @Column(name = "payable_account_id")
    public Long payableAccountId;

    /**
     * Planning lead time used by MRP.
     */
    @Column(name = "lead_time_days")
    public Integer leadTimeDays;

    /**
     * 1-5, maintained by the vendor scorecard.
     */
    @Column(name = "rating")
    public Integer rating;

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
