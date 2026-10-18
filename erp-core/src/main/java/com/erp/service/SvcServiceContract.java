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
 * Service agreement entitling a customer to included hours (M-SVC-007). Overage is billed at
 * the contracted rate.
 */
@Entity(name = "SvcServiceContract")
@Table(name = "svc_service_contract")
public class SvcServiceContract extends BaseEntity {

    /**
     * contractNumber
     */
    @Column(name = "contract_number", length = 40, nullable = false)
    public String contractNumber;

    /**
     * customerId
     */
    @Column(name = "customer_id", nullable = false)
    public Long customerId;

    /**
     * name
     */
    @Column(name = "name", length = 200)
    public String name;

    /**
     * startDate
     */
    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    /**
     * endDate
     */
    @Column(name = "end_date")
    public LocalDate endDate;

    /**
     * includedHours
     */
    @Column(name = "included_hours", precision = 9, scale = 6)
    public BigDecimal includedHours;

    /**
     * consumedHours
     */
    @Column(name = "consumed_hours", precision = 9, scale = 6)
    public BigDecimal consumedHours;

    /**
     * hourlyRate
     */
    @Column(name = "hourly_rate", precision = 19, scale = 4)
    public BigDecimal hourlyRate;

    /**
     * annualFee
     */
    @Column(name = "annual_fee", precision = 19, scale = 4)
    public BigDecimal annualFee;

    /**
     * Maps to an SLA policy priority.
     */
    @Column(name = "response_level", length = 40)
    public String responseLevel;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public ServiceContractStatus status;

    /**
     * autoRenew
     */
    @Column(name = "auto_renew")
    public Boolean autoRenew;

    /**
     * terms
     */
    @Column(name = "terms")
    public String terms;

    @Override
    public String displayLabel() {
        return contractNumber + " - " + name;
    }

}
