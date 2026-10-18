package com.erp.logistics;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Planned route with distance and duration (M-LOG-005).
 */
@Entity(name = "LogRoute")
@Table(name = "log_route")
public class LogRoute extends BaseEntity {

    /**
     * shipmentId
     */
    @Column(name = "shipment_id")
    public Long shipmentId;

    /**
     * routeNumber
     */
    @Column(name = "route_number", length = 40, nullable = false)
    public String routeNumber;

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
     * plannedDeparture
     */
    @Column(name = "planned_departure")
    public LocalDate plannedDeparture;

    /**
     * plannedArrival
     */
    @Column(name = "planned_arrival")
    public LocalDate plannedArrival;

    /**
     * distanceKm
     */
    @Column(name = "distance_km", precision = 9, scale = 6)
    public BigDecimal distanceKm;

    /**
     * durationMinutes
     */
    @Column(name = "duration_minutes")
    public Integer durationMinutes;

    /**
     * vehiclePlate
     */
    @Column(name = "vehicle_plate", length = 40)
    public String vehiclePlate;

    /**
     * driverEmployeeId
     */
    @Column(name = "driver_employee_id")
    public Long driverEmployeeId;

    /**
     * status
     */
    @Column(name = "status", length = 20)
    public String status;

    @Override
    public String displayLabel() {
        return routeNumber;
    }

}
