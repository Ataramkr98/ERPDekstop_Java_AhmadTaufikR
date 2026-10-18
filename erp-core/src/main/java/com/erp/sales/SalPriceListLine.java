package com.erp.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Tiered price break (M-SAL-004). Volume pricing is expressed as one row per minimum quantity.
 */
@Entity(name = "SalPriceListLine")
@Table(name = "sal_price_list_line")
public class SalPriceListLine extends BaseEntity {

    /**
     * priceListId
     */
    @Column(name = "price_list_id", nullable = false)
    public Long priceListId;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * minQuantity
     */
    @Column(name = "min_quantity", precision = 9, scale = 6)
    public BigDecimal minQuantity;

    /**
     * unitPrice
     */
    @Column(name = "unit_price", precision = 19, scale = 4)
    public BigDecimal unitPrice;

    /**
     * discountPercent
     */
    @Column(name = "discount_percent", precision = 9, scale = 6)
    public BigDecimal discountPercent;

    /**
     * validFrom
     */
    @Column(name = "valid_from")
    public LocalDate validFrom;

    /**
     * validTo
     */
    @Column(name = "valid_to")
    public LocalDate validTo;

}
