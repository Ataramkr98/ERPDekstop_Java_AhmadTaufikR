package com.erp.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Lot with expiry and attributes (M-INV-004). FEFO picking and shelf-life reporting are driven
 * from these fields.
 */
@Entity(name = "InvLot")
@Table(name = "inv_lot")
public class InvLot extends BaseEntity {

    /**
     * lotNumber
     */
    @Column(name = "lot_number", length = 60, nullable = false)
    public String lotNumber;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * manufacturingDate
     */
    @Column(name = "manufacturing_date")
    public LocalDate manufacturingDate;

    /**
     * expiryDate
     */
    @Column(name = "expiry_date")
    public LocalDate expiryDate;

    /**
     * supplierLotNumber
     */
    @Column(name = "supplier_lot_number", length = 60)
    public String supplierLotNumber;

    /**
     * vendorId
     */
    @Column(name = "vendor_id")
    public Long vendorId;

    /**
     * quantity
     */
    @Column(name = "quantity", precision = 9, scale = 6)
    public BigDecimal quantity;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public LotStatus status;

    /**
     * potency
     */
    @Column(name = "potency", length = 40)
    public String potency;

    /**
     * grade
     */
    @Column(name = "grade", length = 40)
    public String grade;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return lotNumber;
    }

}
