package com.erp.crm;

import java.math.BigDecimal;
import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Lead captured from a web form, an email, an import or manual entry (M-CRM-001). Conversion
 * creates the Account, the Contact and optionally an Opportunity.
 */
@Entity(name = "CrmLead")
@Table(name = "crm_lead")
public class CrmLead extends BaseEntity {

    /**
     * leadNumber
     */
    @Column(name = "lead_number", length = 40, nullable = false)
    public String leadNumber;

    /**
     * company
     */
    @Column(name = "company", length = 200, nullable = false)
    public String company;

    /**
     * contactName
     */
    @Column(name = "contact_name", length = 160)
    public String contactName;

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
     * source
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 40)
    public LeadSource source;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public LeadStatus status;

    /**
     * 0-100 lead score; higher is hotter.
     */
    @Column(name = "score")
    public Integer score;

    /**
     * estimatedValue
     */
    @Column(name = "estimated_value", precision = 19, scale = 4)
    public BigDecimal estimatedValue;

    /**
     * industry
     */
    @Column(name = "industry", length = 60)
    public String industry;

    /**
     * city
     */
    @Column(name = "city", length = 100)
    public String city;

    /**
     * countryCode
     */
    @Column(name = "country_code", length = 2)
    public String countryCode;

    /**
     * campaignId
     */
    @Column(name = "campaign_id")
    public Long campaignId;

    /**
     * ownerEmployeeId
     */
    @Column(name = "owner_employee_id")
    public Long ownerEmployeeId;

    /**
     * convertedAccountId
     */
    @Column(name = "converted_account_id")
    public Long convertedAccountId;

    /**
     * convertedContactId
     */
    @Column(name = "converted_contact_id")
    public Long convertedContactId;

    /**
     * convertedOpportunityId
     */
    @Column(name = "converted_opportunity_id")
    public Long convertedOpportunityId;

    /**
     * convertedAt
     */
    @Column(name = "converted_at")
    public Instant convertedAt;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return leadNumber + " - " + company;
    }

}
