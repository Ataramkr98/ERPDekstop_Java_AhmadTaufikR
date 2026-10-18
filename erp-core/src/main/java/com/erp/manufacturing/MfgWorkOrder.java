package com.erp.manufacturing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Work order (M-MFG-003). Linked to a sales order for make-to-order or to a forecast for
 * make-to-stock.
 */
@Entity(name = "MfgWorkOrder")
@Table(name = "mfg_work_order")
public class MfgWorkOrder extends BaseEntity {

    /**
     * workOrderNumber
     */
    @Column(name = "work_order_number", length = 40, nullable = false)
    public String workOrderNumber;

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * bomId
     */
    @Column(name = "bom_id")
    public Long bomId;

    /**
     * routingId
     */
    @Column(name = "routing_id")
    public Long routingId;

    /**
     * quantity
     */
    @Column(name = "quantity", precision = 9, scale = 6)
    public BigDecimal quantity;

    /**
     * quantityCompleted
     */
    @Column(name = "quantity_completed", precision = 9, scale = 6)
    public BigDecimal quantityCompleted;

    /**
     * quantityScrapped
     */
    @Column(name = "quantity_scrapped", precision = 9, scale = 6)
    public BigDecimal quantityScrapped;

    /**
     * plannedStartDate
     */
    @Column(name = "planned_start_date")
    public LocalDate plannedStartDate;

    /**
     * plannedEndDate
     */
    @Column(name = "planned_end_date")
    public LocalDate plannedEndDate;

    /**
     * actualStartDate
     */
    @Column(name = "actual_start_date")
    public LocalDate actualStartDate;

    /**
     * actualEndDate
     */
    @Column(name = "actual_end_date")
    public LocalDate actualEndDate;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public WorkOrderStatus status;

    /**
     * 1 is highest.
     */
    @Column(name = "priority")
    public Integer priority;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * salesOrderId
     */
    @Column(name = "sales_order_id")
    public Long salesOrderId;

    /**
     * plannedOrderId
     */
    @Column(name = "planned_order_id")
    public Long plannedOrderId;

    /**
     * plannedCost
     */
    @Column(name = "planned_cost", precision = 19, scale = 4)
    public BigDecimal plannedCost;

    /**
     * actualCost
     */
    @Column(name = "actual_cost", precision = 19, scale = 4)
    public BigDecimal actualCost;

    /**
     * materialCost
     */
    @Column(name = "material_cost", precision = 19, scale = 4)
    public BigDecimal materialCost;

    /**
     * laborCost
     */
    @Column(name = "labor_cost", precision = 19, scale = 4)
    public BigDecimal laborCost;

    /**
     * overheadCost
     */
    @Column(name = "overhead_cost", precision = 19, scale = 4)
    public BigDecimal overheadCost;

    /**
     * varianceAmount
     */
    @Column(name = "variance_amount", precision = 19, scale = 4)
    public BigDecimal varianceAmount;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return workOrderNumber;
    }

}
