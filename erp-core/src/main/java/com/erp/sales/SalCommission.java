package com.erp.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Accrued commission per sales rep (M-SAL-010). Accrues at invoice time and pays out in a
 * monthly run that produces a per-rep statement.
 */
@Entity(name = "SalCommission")
@Table(name = "sal_commission")
public class SalCommission extends BaseEntity {

    /**
     * salespersonId
     */
    @Column(name = "salesperson_id", nullable = false)
    public Long salespersonId;

    /**
     * invoiceId
     */
    @Column(name = "invoice_id")
    public Long invoiceId;

    /**
     * orderId
     */
    @Column(name = "order_id")
    public Long orderId;

    /**
     * periodYear
     */
    @Column(name = "period_year")
    public Integer periodYear;

    /**
     * periodMonth
     */
    @Column(name = "period_month")
    public Integer periodMonth;

    /**
     * Invoice amount the rate applies to.
     */
    @Column(name = "base_amount", precision = 19, scale = 4)
    public BigDecimal baseAmount;

    /**
     * Commission rate as a fraction.
     */
    @Column(name = "rate", precision = 9, scale = 6)
    public BigDecimal rate;

    /**
     * commissionAmount
     */
    @Column(name = "commission_amount", precision = 19, scale = 4)
    public BigDecimal commissionAmount;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public CommissionStatus status;

    /**
     * paidDate
     */
    @Column(name = "paid_date")
    public LocalDate paidDate;

    /**
     * payoutRunId
     */
    @Column(name = "payout_run_id")
    public Long payoutRunId;

}
