package com.erp.crm;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Person at an account with a role (M-CRM-003). Communication preferences gate email and SMS
 * sends.
 */
@Entity(name = "CrmContact")
@Table(name = "crm_contact")
public class CrmContact extends BaseEntity {

    /**
     * accountId
     */
    @Column(name = "account_id")
    public Long accountId;

    /**
     * firstName
     */
    @Column(name = "first_name", length = 120, nullable = false)
    public String firstName;

    /**
     * lastName
     */
    @Column(name = "last_name", length = 120)
    public String lastName;

    /**
     * title
     */
    @Column(name = "title", length = 80)
    public String title;

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
     * mobile
     */
    @Column(name = "mobile", length = 40)
    public String mobile;

    /**
     * DECISION_MAKER, INFLUENCER, END_USER, FINANCE, TECHNICAL.
     */
    @Column(name = "role", length = 40)
    public String role;

    /**
     * Marks the primary contact for the account (M-CRM-003).
     */
    @Column(name = "is_primary")
    public Boolean isPrimary;

    /**
     * doNotCall
     */
    @Column(name = "do_not_call")
    public Boolean doNotCall;

    /**
     * emailOptOut
     */
    @Column(name = "email_opt_out")
    public Boolean emailOptOut;

    /**
     * customerId
     */
    @Column(name = "customer_id")
    public Long customerId;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return firstName + " " + lastName;
    }

}
