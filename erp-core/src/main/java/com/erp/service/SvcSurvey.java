package com.erp.service;

import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Post-resolution CSAT survey (M-SVC-008). Scores are aggregated per period for the service
 * quality trend.
 */
@Entity(name = "SvcSurvey")
@Table(name = "svc_survey")
public class SvcSurvey extends BaseEntity {

    /**
     * ticketId
     */
    @Column(name = "ticket_id", nullable = false)
    public Long ticketId;

    /**
     * customerId
     */
    @Column(name = "customer_id")
    public Long customerId;

    /**
     * sentDate
     */
    @Column(name = "sent_date")
    public LocalDate sentDate;

    /**
     * responseDate
     */
    @Column(name = "response_date")
    public LocalDate responseDate;

    /**
     * 1-5 satisfaction score.
     */
    @Column(name = "score")
    public Integer score;

    /**
     * POSITIVE, NEUTRAL or NEGATIVE.
     */
    @Column(name = "sentiment", length = 20)
    public String sentiment;

    /**
     * comment
     */
    @Column(name = "comment")
    public String comment;

}
