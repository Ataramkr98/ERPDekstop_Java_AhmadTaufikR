package com.erp.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Claim against a warranty (M-SVC-005). Coverage is checked automatically when the claim is
 * raised.
 */
@Entity(name = "SvcWarrantyClaim")
@Table(name = "svc_warranty_claim")
public class SvcWarrantyClaim extends BaseEntity {

    /**
     * claimNumber
     */
    @Column(name = "claim_number", length = 40, nullable = false)
    public String claimNumber;

    /**
     * warrantyId
     */
    @Column(name = "warranty_id", nullable = false)
    public Long warrantyId;

    /**
     * ticketId
     */
    @Column(name = "ticket_id")
    public Long ticketId;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * serialId
     */
    @Column(name = "serial_id")
    public Long serialId;

    /**
     * customerId
     */
    @Column(name = "customer_id")
    public Long customerId;

    /**
     * claimDate
     */
    @Column(name = "claim_date", nullable = false)
    public LocalDate claimDate;

    /**
     * defectCode
     */
    @Column(name = "defect_code", length = 40)
    public String defectCode;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public WarrantyClaimStatus status;

    /**
     * covered
     */
    @Column(name = "covered")
    public Boolean covered;

    /**
     * claimAmount
     */
    @Column(name = "claim_amount", precision = 19, scale = 4)
    public BigDecimal claimAmount;

    /**
     * approvedAmount
     */
    @Column(name = "approved_amount", precision = 19, scale = 4)
    public BigDecimal approvedAmount;

    /**
     * resolutionNotes
     */
    @Column(name = "resolution_notes")
    public String resolutionNotes;

    @Override
    public String displayLabel() {
        return claimNumber;
    }

}
