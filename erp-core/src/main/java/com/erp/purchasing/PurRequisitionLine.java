package com.erp.purchasing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Requisition line (M-PUR-002).
 */
@Entity(name = "PurRequisitionLine")
@Table(name = "pur_requisition_line")
public class PurRequisitionLine extends BaseEntity {

    /**
     * requisitionId
     */
    @Column(name = "requisition_id", nullable = false)
    public Long requisitionId;

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
     * description
     */
    @Column(name = "description")
    public String description;

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
     * estimatedUnitPrice
     */
    @Column(name = "estimated_unit_price", precision = 19, scale = 4)
    public BigDecimal estimatedUnitPrice;

    /**
     * estimatedTotal
     */
    @Column(name = "estimated_total", precision = 19, scale = 4)
    public BigDecimal estimatedTotal;

    /**
     * requiredDate
     */
    @Column(name = "required_date")
    public LocalDate requiredDate;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * Suggested vendor, carried into the RFQ.
     */
    @Column(name = "vendor_id")
    public Long vendorId;

}
