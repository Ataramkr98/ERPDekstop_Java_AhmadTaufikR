package com.erp.sales;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Customer invoice line.
 */
@Entity(name = "SalInvoiceLine")
@Table(name = "sal_invoice_line")
public class SalInvoiceLine extends BaseEntity {

    /**
     * invoiceId
     */
    @Column(name = "invoice_id", nullable = false)
    public Long invoiceId;

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
     * taxAmount
     */
    @Column(name = "tax_amount", precision = 19, scale = 4)
    public BigDecimal taxAmount;

    /**
     * lineTotal
     */
    @Column(name = "line_total", precision = 19, scale = 4)
    public BigDecimal lineTotal;

    /**
     * revenueAccountId
     */
    @Column(name = "revenue_account_id")
    public Long revenueAccountId;

}
