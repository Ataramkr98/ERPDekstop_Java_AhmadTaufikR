package com.erp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Fixed asset register (M-FIN-006). An asset can be created from a purchase receipt or
 * manually; the monthly depreciation run posts journals.
 */
@Entity(name = "FinFixedAsset")
@Table(name = "fin_fixed_asset")
public class FinFixedAsset extends BaseEntity {

    /**
     * assetNumber
     */
    @Column(name = "asset_number", length = 40, nullable = false)
    public String assetNumber;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    /**
     * category
     */
    @Column(name = "category", length = 60)
    public String category;

    /**
     * acquisitionDate
     */
    @Column(name = "acquisition_date")
    public LocalDate acquisitionDate;

    /**
     * acquisitionCost
     */
    @Column(name = "acquisition_cost", precision = 19, scale = 4)
    public BigDecimal acquisitionCost;

    /**
     * salvageValue
     */
    @Column(name = "salvage_value", precision = 19, scale = 4)
    public BigDecimal salvageValue;

    /**
     * usefulLifeMonths
     */
    @Column(name = "useful_life_months")
    public Integer usefulLifeMonths;

    /**
     * depreciationMethod
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", length = 40)
    public DepreciationMethod depreciationMethod;

    /**
     * Multiplier for the declining balance method.
     */
    @Column(name = "depreciation_rate", precision = 9, scale = 6)
    public BigDecimal depreciationRate;

    /**
     * accumulatedDepreciation
     */
    @Column(name = "accumulated_depreciation", precision = 19, scale = 4)
    public BigDecimal accumulatedDepreciation;

    /**
     * netBookValue
     */
    @Column(name = "net_book_value", precision = 19, scale = 4)
    public BigDecimal netBookValue;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public AssetStatus status;

    /**
     * glAccountId
     */
    @Column(name = "gl_account_id")
    public Long glAccountId;

    /**
     * accumulatedDepreciationAccountId
     */
    @Column(name = "accumulated_depreciation_account_id")
    public Long accumulatedDepreciationAccountId;

    /**
     * depreciationExpenseAccountId
     */
    @Column(name = "depreciation_expense_account_id")
    public Long depreciationExpenseAccountId;

    /**
     * costCenterId
     */
    @Column(name = "cost_center_id")
    public Long costCenterId;

    /**
     * vendorId
     */
    @Column(name = "vendor_id")
    public Long vendorId;

    /**
     * disposalDate
     */
    @Column(name = "disposal_date")
    public LocalDate disposalDate;

    /**
     * disposalAmount
     */
    @Column(name = "disposal_amount", precision = 19, scale = 4)
    public BigDecimal disposalAmount;

    /**
     * gainLossOnDisposal
     */
    @Column(name = "gain_loss_on_disposal", precision = 19, scale = 4)
    public BigDecimal gainLossOnDisposal;

    /**
     * lastDepreciationDate
     */
    @Column(name = "last_depreciation_date")
    public LocalDate lastDepreciationDate;

    @Override
    public String displayLabel() {
        return assetNumber + " - " + name;
    }

}
