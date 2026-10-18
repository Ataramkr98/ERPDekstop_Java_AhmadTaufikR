package com.erp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Daily exchange rate table (M-FIN-007). Period-end revaluation is a background job that reads
 * the CLOSING rate and posts unrealised gain or loss.
 */
@Entity(name = "FinExchangeRate")
@Table(name = "fin_exchange_rate")
public class FinExchangeRate extends BaseEntity {

    /**
     * fromCurrency
     */
    @Column(name = "from_currency", length = 3, nullable = false)
    public String fromCurrency;

    /**
     * toCurrency
     */
    @Column(name = "to_currency", length = 3, nullable = false)
    public String toCurrency;

    /**
     * rateDate
     */
    @Column(name = "rate_date", nullable = false)
    public LocalDate rateDate;

    /**
     * rateType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", length = 40)
    public RateType rateType;

    /**
     * Units of toCurrency per one unit of fromCurrency.
     */
    @Column(name = "rate", precision = 9, scale = 6, nullable = false)
    public BigDecimal rate;

    /**
     * e.g. BI reference rate, manual entry, bank feed.
     */
    @Column(name = "source", length = 60)
    public String source;

    @Override
    public String displayLabel() {
        return fromCurrency + "/" + toCurrency;
    }

}
