package com.erp.logistics;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Contents of a shipment (M-LOG-001).
 */
@Entity(name = "LogShipmentLine")
@Table(name = "log_shipment_line")
public class LogShipmentLine extends BaseEntity {

    /**
     * shipmentId
     */
    @Column(name = "shipment_id", nullable = false)
    public Long shipmentId;

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
     * deliveryLineId
     */
    @Column(name = "delivery_line_id")
    public Long deliveryLineId;

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
     * weight
     */
    @Column(name = "weight", precision = 9, scale = 6)
    public BigDecimal weight;

    /**
     * volume
     */
    @Column(name = "volume", precision = 9, scale = 6)
    public BigDecimal volume;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

}
