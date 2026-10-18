package com.erp.sales;

import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Delivery note (M-SAL-008). Ship confirm decrements stock and posts the COGS and inventory
 * journals in one transaction.
 */
@Entity(name = "SalDelivery")
@Table(name = "sal_delivery")
public class SalDelivery extends BaseEntity {

    /**
     * deliveryNumber
     */
    @Column(name = "delivery_number", length = 40, nullable = false)
    public String deliveryNumber;

    /**
     * orderId
     */
    @Column(name = "order_id")
    public Long orderId;

    /**
     * customerId
     */
    @Column(name = "customer_id", nullable = false)
    public Long customerId;

    /**
     * deliveryDate
     */
    @Column(name = "delivery_date", nullable = false)
    public LocalDate deliveryDate;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public DeliveryStatus status = DeliveryStatus.PENDING;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * trackingNumber
     */
    @Column(name = "tracking_number", length = 120)
    public String trackingNumber;

    /**
     * Logistics shipment this delivery rides on (M-LOG-001).
     */
    @Column(name = "shipment_id")
    public Long shipmentId;

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
        return deliveryNumber;
    }

}
