package com.erp.logistics;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Shipment header (M-LOG-001). Created from a delivery for outbound freight or from an advance
 * ship notice for inbound.
 */
@Entity(name = "LogShipment")
@Table(name = "log_shipment")
public class LogShipment extends BaseEntity {

    /**
     * shipmentNumber
     */
    @Column(name = "shipment_number", length = 40, nullable = false)
    public String shipmentNumber;

    /**
     * OUTBOUND or INBOUND.
     */
    @Column(name = "direction", length = 10)
    public String direction;

    /**
     * carrierId
     */
    @Column(name = "carrier_id")
    public Long carrierId;

    /**
     * deliveryId
     */
    @Column(name = "delivery_id")
    public Long deliveryId;

    /**
     * orderId
     */
    @Column(name = "order_id")
    public Long orderId;

    /**
     * vendorId
     */
    @Column(name = "vendor_id")
    public Long vendorId;

    /**
     * transportMode
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", length = 40)
    public TransportMode transportMode;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public ShipmentStatus status;

    /**
     * shipDate
     */
    @Column(name = "ship_date")
    public LocalDate shipDate;

    /**
     * expectedDeliveryDate
     */
    @Column(name = "expected_delivery_date")
    public LocalDate expectedDeliveryDate;

    /**
     * actualDeliveryDate
     */
    @Column(name = "actual_delivery_date")
    public LocalDate actualDeliveryDate;

    /**
     * trackingNumber
     */
    @Column(name = "tracking_number", length = 120)
    public String trackingNumber;

    /**
     * originCity
     */
    @Column(name = "origin_city", length = 100)
    public String originCity;

    /**
     * destinationCity
     */
    @Column(name = "destination_city", length = 100)
    public String destinationCity;

    /**
     * originCountryCode
     */
    @Column(name = "origin_country_code", length = 2)
    public String originCountryCode;

    /**
     * destinationCountryCode
     */
    @Column(name = "destination_country_code", length = 2)
    public String destinationCountryCode;

    /**
     * totalWeight
     */
    @Column(name = "total_weight", precision = 9, scale = 6)
    public BigDecimal totalWeight;

    /**
     * totalVolume
     */
    @Column(name = "total_volume", precision = 9, scale = 6)
    public BigDecimal totalVolume;

    /**
     * packageCount
     */
    @Column(name = "package_count")
    public Integer packageCount;

    /**
     * declaredValue
     */
    @Column(name = "declared_value", precision = 19, scale = 4)
    public BigDecimal declaredValue;

    /**
     * B/L for sea freight.
     */
    @Column(name = "bill_of_lading_number", length = 80)
    public String billOfLadingNumber;

    /**
     * AWB for air freight (M-LOG-007).
     */
    @Column(name = "air_waybill_number", length = 80)
    public String airWaybillNumber;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return shipmentNumber;
    }

}
