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
 * Marketing campaign (M-CRM). Campaign ROI is reported against attributed revenue.
 */
@Entity(name = "CrmCampaign")
@Table(name = "crm_campaign")
public class CrmCampaign extends BaseEntity {

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
     * EMAIL, SOCIAL, EVENT, WEBINAR, DIRECT_MAIL.
     */
    @Column(name = "channel", length = 40)
    public String channel;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public CampaignStatus status;

    /**
     * startDate
     */
    @Column(name = "start_date")
    public LocalDate startDate;

    /**
     * endDate
     */
    @Column(name = "end_date")
    public LocalDate endDate;

    /**
     * budgetCost
     */
    @Column(name = "budget_cost", precision = 19, scale = 4)
    public BigDecimal budgetCost;

    /**
     * attributedRevenue
     */
    @Column(name = "attributed_revenue", precision = 19, scale = 4)
    public BigDecimal attributedRevenue;

    /**
     * leadCount
     */
    @Column(name = "lead_count")
    public Integer leadCount;

    /**
     * convertedCount
     */
    @Column(name = "converted_count")
    public Integer convertedCount;

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
