package com.erp.platform;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Legal entity. Every tenant-scoped row in the system carries a company_id and reports default
 * to the caller's active company (M-ADM-003, M-ADM-008).
 */
@Entity(name = "Company")
@Table(name = "company")
public class Company extends BaseEntity {

    /**
     * Short unique code used in number ranges and imports.
     */
    @Column(name = "code", length = 20, nullable = false)
    public String code;

    /**
     * Trading name shown in the company switcher.
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * Registered name used on statutory reports and tax forms.
     */
    @Column(name = "legal_name", length = 200)
    public String legalName;

    /**
     * NPWP / VAT / EIN registration number.
     */
    @Column(name = "tax_id", length = 40)
    public String taxId;

    /**
     * Company registration number.
     */
    @Column(name = "registration_no", length = 60)
    public String registrationNo;

    /**
     * ISO 4217 code of the ledger currency (M-ADM-004).
     */
    @Column(name = "base_currency", length = 3)
    public String baseCurrency;

    /**
     * ISO 4217 code used for consolidated reporting.
     */
    @Column(name = "reporting_currency", length = 3)
    public String reportingCurrency;

    /**
     * ISO 3166-1 alpha-2, drives the statutory packs.
     */
    @Column(name = "country_code", length = 2)
    public String countryCode;

    /**
     * Default UI locale, e.g. en-US or id-ID (M-ADM-005).
     */
    @Column(name = "locale_tag", length = 16)
    public String localeTag;

    /**
     * IANA zone used to render local business dates.
     */
    @Column(name = "timezone", length = 64)
    public String timezone;

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
     * phone
     */
    @Column(name = "phone", length = 40)
    public String phone;

    /**
     * email
     */
    @Column(name = "email", length = 120)
    public String email;

    /**
     * website
     */
    @Column(name = "website", length = 200)
    public String website;

    /**
     * 1-12; supports a non-calendar fiscal year.
     */
    @Column(name = "fiscal_year_start_month")
    public Integer fiscalYearStartMonth;

    /**
     * Warehouse pre-selected on inventory transactions.
     */
    @Column(name = "default_warehouse_id")
    public Long defaultWarehouseId;

    /**
     * Optional shared CoA across companies (M-ADM-003).
     */
    @Column(name = "chart_of_accounts_id")
    public Long chartOfAccountsId;

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
