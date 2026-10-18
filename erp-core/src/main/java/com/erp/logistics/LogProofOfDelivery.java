package com.erp.logistics;

import java.math.BigDecimal;
import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Proof of delivery with signature, photo and timestamp (M-LOG-006).
 */
@Entity(name = "LogProofOfDelivery")
@Table(name = "log_proof_of_delivery")
public class LogProofOfDelivery extends BaseEntity {

    /**
     * shipmentId
     */
    @Column(name = "shipment_id", nullable = false)
    public Long shipmentId;

    /**
     * deliveredAt
     */
    @Column(name = "delivered_at")
    public Instant deliveredAt;

    /**
     * receivedBy
     */
    @Column(name = "received_by", length = 160)
    public String receivedBy;

    /**
     * Path to the captured signature image.
     */
    @Column(name = "signature_reference", length = 300)
    public String signatureReference;

    /**
     * Path to the captured photo.
     */
    @Column(name = "photo_reference", length = 300)
    public String photoReference;

    /**
     * latitude
     */
    @Column(name = "latitude", length = 40)
    public String latitude;

    /**
     * longitude
     */
    @Column(name = "longitude", length = 40)
    public String longitude;

    /**
     * Cold chain reading when the shipment is monitored.
     */
    @Column(name = "temperature_reading", precision = 9, scale = 6)
    public BigDecimal temperatureReading;

    /**
     * damaged
     */
    @Column(name = "damaged")
    public Boolean damaged;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
