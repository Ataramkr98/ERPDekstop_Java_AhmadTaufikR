package com.erp.manufacturing;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Component line of a BOM (M-MFG-001). Scrap percent inflates the planned quantity.
 */
@Entity(name = "MfgBomLine")
@Table(name = "mfg_bom_line")
public class MfgBomLine extends BaseEntity {

    /**
     * bomId
     */
    @Column(name = "bom_id", nullable = false)
    public Long bomId;

    /**
     * lineNo
     */
    @Column(name = "line_no", nullable = false)
    public Integer lineNo;

    /**
     * componentItemId
     */
    @Column(name = "component_item_id", nullable = false)
    public Long componentItemId;

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
     * scrapPercent
     */
    @Column(name = "scrap_percent", precision = 9, scale = 6)
    public BigDecimal scrapPercent;

    /**
     * isPhantom
     */
    @Column(name = "is_phantom")
    public Boolean isPhantom;

    /**
     * By-products come out of the process rather than going in.
     */
    @Column(name = "is_by_product")
    public Boolean isByProduct;

    /**
     * Components sharing a group are alternates.
     */
    @Column(name = "alternate_group_id")
    public Long alternateGroupId;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * level
     */
    @Column(name = "level")
    public Integer level;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

}
