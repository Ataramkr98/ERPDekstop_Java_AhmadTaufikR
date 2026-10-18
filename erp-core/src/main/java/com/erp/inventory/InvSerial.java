package com.erp.inventory;

import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Serialised unit (M-INV-005). Captured at goods receipt, validated at ship, and linked to
 * warranty claims in the Service module.
 */
@Entity(name = "InvSerial")
@Table(name = "inv_serial")
public class InvSerial extends BaseEntity {

    /**
     * serialNumber
     */
    @Column(name = "serial_number", length = 120, nullable = false)
    public String serialNumber;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * lotId
     */
    @Column(name = "lot_id")
    public Long lotId;

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
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public SerialStatus status;

    /**
     * receivedDate
     */
    @Column(name = "received_date")
    public LocalDate receivedDate;

    /**
     * shippedDate
     */
    @Column(name = "shipped_date")
    public LocalDate shippedDate;

    /**
     * customerId
     */
    @Column(name = "customer_id")
    public Long customerId;

    /**
     * warrantyExpiryDate
     */
    @Column(name = "warranty_expiry_date")
    public LocalDate warrantyExpiryDate;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return serialNumber;
    }

}
