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
 * Configurable tax code (M-FIN-008). An administrator defines a code, attaches it to a
 * jurisdiction and it becomes selectable on every relevant transaction.
 */
@Entity(name = "FinTaxCode")
@Table(name = "fin_tax_code")
public class FinTaxCode extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 30, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 120, nullable = false)
    public String name;

    /**
     * taxType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", length = 40)
    public TaxType taxType;

    /**
     * Effective rate as a fraction, e.g. 0.11 for 11% VAT.
     */
    @Column(name = "rate", precision = 9, scale = 6)
    public BigDecimal rate;

    /**
     * jurisdiction
     */
    @Column(name = "jurisdiction", length = 60)
    public String jurisdiction;

    /**
     * GL account the tax is posted to.
     */
    @Column(name = "tax_account_id")
    public Long taxAccountId;

    /**
     * True when quoted prices already include this tax.
     */
    @Column(name = "price_inclusive")
    public Boolean priceInclusive;

    /**
     * False for non-creditable withholding taxes.
     */
    @Column(name = "recoverable")
    public Boolean recoverable;

    /**
     * effectiveFrom
     */
    @Column(name = "effective_from")
    public LocalDate effectiveFrom;

    /**
     * effectiveTo
     */
    @Column(name = "effective_to")
    public LocalDate effectiveTo;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
