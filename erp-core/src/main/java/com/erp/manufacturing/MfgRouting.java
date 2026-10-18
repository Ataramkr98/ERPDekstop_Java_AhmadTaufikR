package com.erp.manufacturing;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Routing header (M-MFG-002). Defines the ordered operations for producing an item.
 */
@Entity(name = "MfgRouting")
@Table(name = "mfg_routing")
public class MfgRouting extends BaseEntity {

    /**
     * routingCode
     */
    @Column(name = "routing_code", length = 40, nullable = false)
    public String routingCode;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * revision
     */
    @Column(name = "revision", length = 20)
    public String revision;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public RoutingStatus status;

    /**
     * totalSetupMinutes
     */
    @Column(name = "total_setup_minutes", precision = 9, scale = 6)
    public BigDecimal totalSetupMinutes;

    /**
     * totalRunMinutes
     */
    @Column(name = "total_run_minutes", precision = 9, scale = 6)
    public BigDecimal totalRunMinutes;

    /**
     * Rolled up from operations for the standard cost.
     */
    @Column(name = "total_cost", precision = 19, scale = 4)
    public BigDecimal totalCost;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return routingCode;
    }

}
