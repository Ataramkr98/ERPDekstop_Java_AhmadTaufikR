package com.erp.service;

import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Per-serial warranty (M-SVC-005). A claim checks coverage against these dates.
 */
@Entity(name = "SvcWarranty")
@Table(name = "svc_warranty")
public class SvcWarranty extends BaseEntity {

    /**
     * warrantyNumber
     */
    @Column(name = "warranty_number", length = 40, nullable = false)
    public String warrantyNumber;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
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
     * durationMonths
     */
    @Column(name = "duration_months")
    public Integer durationMonths;

    /**
     * warrantyType
     */
    @Column(name = "warranty_type", length = 30)
    public String warrantyType;

    /**
     * coverageTerms
     */
    @Column(name = "coverage_terms", length = 500)
    public String coverageTerms;

    @Override
    public String displayLabel() {
        return warrantyNumber;
    }

}
