package com.erp.purchasing;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Vendor invoice line (M-PUR-008).
 */
@Entity(name = "PurInvoiceLine")
@Table(name = "pur_invoice_line")
public class PurInvoiceLine extends BaseEntity {

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
     * orderLineId
     */
    @Column(name = "order_line_id")
    public Long orderLineId;

    /**
     * receiptLineId
     */
    @Column(name = "receipt_line_id")
    public Long receiptLineId;

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
     * expenseAccountId
     */
    @Column(name = "expense_account_id")
    public Long expenseAccountId;

}
