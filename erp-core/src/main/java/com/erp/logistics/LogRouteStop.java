package com.erp.logistics;

import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One stop on a route (M-LOG-005).
 */
@Entity(name = "LogRouteStop")
@Table(name = "log_route_stop")
public class LogRouteStop extends BaseEntity {

    /**
     * routeId
     */
    @Column(name = "route_id", nullable = false)
    public Long routeId;

    /**
     * stopNo
     */
    @Column(name = "stop_no", nullable = false)
    public Integer stopNo;

    /**
     * address
     */
    @Column(name = "address", length = 400)
    public String address;

    /**
     * city
     */
    @Column(name = "city", length = 100)
    public String city;

    /**
     * shipmentId
     */
    @Column(name = "shipment_id")
    public Long shipmentId;

    /**
     * customerId
     */
    @Column(name = "customer_id")
    public Long customerId;

    /**
     * arrivedAt
     */
    @Column(name = "arrived_at")
    public Instant arrivedAt;

    /**
     * departedAt
     */
    @Column(name = "departed_at")
    public Instant departedAt;

    /**
     * status
     */
    @Column(name = "status", length = 20)
    public String status;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
