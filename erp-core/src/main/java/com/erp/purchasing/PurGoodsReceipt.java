package com.erp.purchasing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Goods receipt against a PO (M-PUR-004). Posting increases stock and credits the
 * goods-received-not-invoiced clearing account.
 */
@Entity(name = "PurGoodsReceipt")
@Table(name = "pur_goods_receipt")
public class PurGoodsReceipt extends BaseEntity {

    /**
     * receiptNumber
     */
    @Column(name = "receipt_number", length = 40, nullable = false)
    public String receiptNumber;

    /**
     * vendorId
     */
    @Column(name = "vendor_id", nullable = false)
    public Long vendorId;

    /**
     * orderId
     */
    @Column(name = "order_id")
    public Long orderId;

    /**
     * receiptDate
     */
    @Column(name = "receipt_date", nullable = false)
    public LocalDate receiptDate;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public GoodsReceiptStatus status = GoodsReceiptStatus.DRAFT;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * Vendor's own delivery note reference.
     */
    @Column(name = "delivery_note_number", length = 80)
    public String deliveryNoteNumber;

    /**
     * totalValue
     */
    @Column(name = "total_value", precision = 19, scale = 4)
    public BigDecimal totalValue;

    /**
     * journalEntryId
     */
    @Column(name = "journal_entry_id")
    public Long journalEntryId;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return receiptNumber;
    }

}
