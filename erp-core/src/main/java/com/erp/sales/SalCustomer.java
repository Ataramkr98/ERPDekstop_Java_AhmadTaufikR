package com.erp.sales;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Customer master (M-SAL-001). Credit limit is checked at order entry and a customer on credit
 * hold is blocked unless an override workflow is used.
 */
@Entity(name = "SalCustomer")
@Table(name = "sal_customer")
public class SalCustomer extends BaseEntity {

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
     * customerType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", length = 40)
    public CustomerType customerType;

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
     * NPWP; required for a compliant e-Faktur.
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
     * Checked against open AR plus the order being entered.
     */
    @Column(name = "credit_limit", precision = 19, scale = 4)
    public BigDecimal creditLimit;

    /**
     * Open AR balance, maintained on invoice and payment.
     */
    @Column(name = "current_balance", precision = 19, scale = 4)
    public BigDecimal currentBalance;

    /**
     * paymentTermDays
     */
    @Column(name = "payment_term_days")
    public Integer paymentTermDays;

    /**
     * priceListId
     */
    @Column(name = "price_list_id")
    public Long priceListId;

    /**
     * Owning sales rep; drives commission (M-SAL-010).
     */
    @Column(name = "salesperson_id")
    public Long salespersonId;

    /**
     * receivableAccountId
     */
    @Column(name = "receivable_account_id")
    public Long receivableAccountId;

    /**
     * isOnCreditHold
     */
    @Column(name = "is_on_credit_hold")
    public Boolean isOnCreditHold;

    /**
     * creditHoldReason
     */
    @Column(name = "credit_hold_reason")
    public String creditHoldReason;

    /**
     * taxExempt
     */
    @Column(name = "tax_exempt")
    public Boolean taxExempt;

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
