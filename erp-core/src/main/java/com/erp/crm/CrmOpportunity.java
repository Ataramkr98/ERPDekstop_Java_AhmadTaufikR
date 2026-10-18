package com.erp.crm;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Deal in the pipeline (M-CRM-004). Shown as a kanban by stage; the expected close date and
 * probability drive the forecast.
 */
@Entity(name = "CrmOpportunity")
@Table(name = "crm_opportunity")
public class CrmOpportunity extends BaseEntity {

    /**
     * opportunityNumber
     */
    @Column(name = "opportunity_number", length = 40, nullable = false)
    public String opportunityNumber;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * accountId
     */
    @Column(name = "account_id")
    public Long accountId;

    /**
     * contactId
     */
    @Column(name = "contact_id")
    public Long contactId;

    /**
     * leadId
     */
    @Column(name = "lead_id")
    public Long leadId;

    /**
     * stage
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", length = 40)
    public OpportunityStage stage;

    /**
     * amount
     */
    @Column(name = "amount", precision = 19, scale = 4)
    public BigDecimal amount;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * 0-1; recalculated on stage change.
     */
    @Column(name = "probability", precision = 9, scale = 6)
    public BigDecimal probability;

    /**
     * weightedAmount
     */
    @Column(name = "weighted_amount", precision = 19, scale = 4)
    public BigDecimal weightedAmount;

    /**
     * expectedCloseDate
     */
    @Column(name = "expected_close_date")
    public LocalDate expectedCloseDate;

    /**
     * actualCloseDate
     */
    @Column(name = "actual_close_date")
    public LocalDate actualCloseDate;

    /**
     * competitor
     */
    @Column(name = "competitor", length = 120)
    public String competitor;

    /**
     * lostReason
     */
    @Column(name = "lost_reason", length = 200)
    public String lostReason;

    /**
     * ownerEmployeeId
     */
    @Column(name = "owner_employee_id")
    public Long ownerEmployeeId;

    /**
     * campaignId
     */
    @Column(name = "campaign_id")
    public Long campaignId;

    /**
     * Quotation raised from this opportunity (M-CRM-009).
     */
    @Column(name = "quotation_id")
    public Long quotationId;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    @Override
    public String displayLabel() {
        return opportunityNumber + " - " + name;
    }

}
