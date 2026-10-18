package com.erp.sales;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Quotation line (M-SAL-002).
 */
@Entity(name = "SalQuotationLine")
@Table(name = "sal_quotation_line")
public class SalQuotationLine extends BaseEntity {

    /**
     * quotationId
     */
    @Column(name = "quotation_id", nullable = false)
    public Long quotationId;

    /**
     * lineNo
     */
    @Column(name = "line_no", nullable = false)
    public Integer lineNo;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * quantity
     */
    @Column(name = "quantity", precision = 9, scale = 6)
    public BigDecimal quantity;

    /**
     * uom
     */
    @Column(name = "uom", length = 20)
    public String uom;

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
     * taxCode
     */
    @Column(name = "tax_code", length = 30)
    public String taxCode;

    /**
     * lineTotal
     */
    @Column(name = "line_total", precision = 19, scale = 4)
    public BigDecimal lineTotal;

}
