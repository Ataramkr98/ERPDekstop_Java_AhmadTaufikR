package com.erp.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Current stock per item, warehouse, location and lot (M-INV-003). on-hand minus reserved
 * equals available-to-promise.
 */
@Entity(name = "InvStock")
@Table(name = "inv_stock")
public class InvStock extends BaseEntity {

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id", nullable = false)
    public Long warehouseId;

    /**
     * locationId
     */
    @Column(name = "location_id")
    public Long locationId;

    /**
     * lotId
     */
    @Column(name = "lot_id")
    public Long lotId;

    /**
     * quantityOnHand
     */
    @Column(name = "quantity_on_hand", precision = 9, scale = 6)
    public BigDecimal quantityOnHand;

    /**
     * Reserved against confirmed sales orders.
     */
    @Column(name = "quantity_reserved", precision = 9, scale = 6)
    public BigDecimal quantityReserved;

    /**
     * On hand minus reserved; this is ATP.
     */
    @Column(name = "quantity_available", precision = 9, scale = 6)
    public BigDecimal quantityAvailable;

    /**
     * quantityInTransit
     */
    @Column(name = "quantity_in_transit", precision = 9, scale = 6)
    public BigDecimal quantityInTransit;

    /**
     * unitCost
     */
    @Column(name = "unit_cost", precision = 19, scale = 4)
    public BigDecimal unitCost;

    /**
     * totalValue
     */
    @Column(name = "total_value", precision = 19, scale = 4)
    public BigDecimal totalValue;

    /**
     * lastMovementDate
     */
    @Column(name = "last_movement_date")
    public LocalDate lastMovementDate;

    /**
     * lastCountDate
     */
    @Column(name = "last_count_date")
    public LocalDate lastCountDate;

}
