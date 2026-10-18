package com.erp.inventory;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Unit of measure conversion for an item (M-INV-001), e.g. 1 BOX = 12 EA.
 */
@Entity(name = "InvItemConversion")
@Table(name = "inv_item_conversion")
public class InvItemConversion extends BaseEntity {

    /**
     * itemId
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * uom
     */
    @Column(name = "uom", length = 20, nullable = false)
    public String uom;

    /**
     * Multiply the primary quantity by this to get this UoM.
     */
    @Column(name = "factor", precision = 9, scale = 6)
    public BigDecimal factor;

    /**
     * isBase
     */
    @Column(name = "is_base")
    public Boolean isBase;

    /**
     * barcode
     */
    @Column(name = "barcode", length = 60)
    public String barcode;

}
