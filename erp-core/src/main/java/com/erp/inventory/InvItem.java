package com.erp.inventory;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Item master (M-INV-001). Holds the primary unit of measure, conversions to alternates, and
 * the valuation method used to cost movements.
 */
@Entity(name = "InvItem")
@Table(name = "inv_item")
public class InvItem extends BaseEntity {

    /**
     * SKU. The natural key used by every import.
     */
    @Column(name = "code", length = 40, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * description
     */
    @Column(name = "description", length = 500)
    public String description;

    /**
     * itemType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 40)
    public ItemType itemType;

    /**
     * category
     */
    @Column(name = "category", length = 60)
    public String category;

    /**
     * Primary unit of measure.
     */
    @Column(name = "uom", length = 20)
    public String uom;

    /**
     * GTIN / EAN / UPC; unique per item.
     */
    @Column(name = "barcode", length = 60)
    public String barcode;

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
     * weightUom
     */
    @Column(name = "weight_uom", length = 10)
    public String weightUom;

    /**
     * standardCost
     */
    @Column(name = "standard_cost", precision = 19, scale = 4)
    public BigDecimal standardCost;

    /**
     * movingAverageCost
     */
    @Column(name = "moving_average_cost", precision = 19, scale = 4)
    public BigDecimal movingAverageCost;

    /**
     * Default selling price.
     */
    @Column(name = "unit_price", precision = 19, scale = 4)
    public BigDecimal unitPrice;

    /**
     * valuationMethod
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "valuation_method", length = 40)
    public ValuationMethod valuationMethod;

    /**
     * taxCode
     */
    @Column(name = "tax_code", length = 30)
    public String taxCode;

    /**
     * inventoryAccountId
     */
    @Column(name = "inventory_account_id")
    public Long inventoryAccountId;

    /**
     * revenueAccountId
     */
    @Column(name = "revenue_account_id")
    public Long revenueAccountId;

    /**
     * COGS account used on delivery.
     */
    @Column(name = "expense_account_id")
    public Long expenseAccountId;

    /**
     * isStockable
     */
    @Column(name = "is_stockable")
    public Boolean isStockable;

    /**
     * isPurchasable
     */
    @Column(name = "is_purchasable")
    public Boolean isPurchasable;

    /**
     * isSaleable
     */
    @Column(name = "is_saleable")
    public Boolean isSaleable;

    /**
     * reorderPoint
     */
    @Column(name = "reorder_point", precision = 9, scale = 6)
    public BigDecimal reorderPoint;

    /**
     * safetyStock
     */
    @Column(name = "safety_stock", precision = 9, scale = 6)
    public BigDecimal safetyStock;

    /**
     * minQuantity
     */
    @Column(name = "min_quantity", precision = 9, scale = 6)
    public BigDecimal minQuantity;

    /**
     * maxQuantity
     */
    @Column(name = "max_quantity", precision = 9, scale = 6)
    public BigDecimal maxQuantity;

    /**
     * leadTimeDays
     */
    @Column(name = "lead_time_days")
    public Integer leadTimeDays;

    /**
     * abcClass
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "abc_class", length = 40)
    public AbcClass abcClass;

    /**
     * Drives FEFO picking when set.
     */
    @Column(name = "shelf_life_days")
    public Integer shelfLifeDays;

    /**
     * lotTracked
     */
    @Column(name = "lot_tracked")
    public Boolean lotTracked;

    /**
     * serialTracked
     */
    @Column(name = "serial_tracked")
    public Boolean serialTracked;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
