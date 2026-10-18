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
 * Immutable journal of every stock change (M-INV-003). Every row has a type, source,
 * destination, reason and the document that caused it.
 */
@Entity(name = "InvMovement")
@Table(name = "inv_movement")
public class InvMovement extends BaseEntity {

    /**
     * movementNumber
     */
    @Column(name = "movement_number", length = 40)
    public String movementNumber;

    /**
     * movementType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", length = 40, nullable = false)
    public MovementType movementType;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * movementDate
     */
    @Column(name = "movement_date", nullable = false)
    public LocalDate movementDate;

    /**
     * Signed: positive increases stock at the destination.
     */
    @Column(name = "quantity", precision = 9, scale = 6)
    public BigDecimal quantity;

    /**
     * uom
     */
    @Column(name = "uom", length = 20)
    public String uom;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * locationId
     */
    @Column(name = "location_id")
    public Long locationId;

    /**
     * destWarehouseId
     */
    @Column(name = "dest_warehouse_id")
    public Long destWarehouseId;

    /**
     * destLocationId
     */
    @Column(name = "dest_location_id")
    public Long destLocationId;

    /**
     * lotId
     */
    @Column(name = "lot_id")
    public Long lotId;

    /**
     * serialNumber
     */
    @Column(name = "serial_number", length = 120)
    public String serialNumber;

    /**
     * unitCost
     */
    @Column(name = "unit_cost", precision = 19, scale = 4)
    public BigDecimal unitCost;

    /**
     * totalCost
     */
    @Column(name = "total_cost", precision = 19, scale = 4)
    public BigDecimal totalCost;

    /**
     * PURCHASE_ORDER, SALES_ORDER, WORK_ORDER, ADJUSTMENT.
     */
    @Column(name = "source_document_type", length = 40)
    public String sourceDocumentType;

    /**
     * sourceDocumentId
     */
    @Column(name = "source_document_id")
    public Long sourceDocumentId;

    /**
     * reasonCode
     */
    @Column(name = "reason_code", length = 40)
    public String reasonCode;

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
        return movementNumber;
    }

}
