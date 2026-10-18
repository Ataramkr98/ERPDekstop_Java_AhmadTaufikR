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
 * Bill of materials header (M-MFG-001). Multi-level with alternates, by-products and scrap
 * percentages; a phantom assembly explodes in the BOM tree.
 */
@Entity(name = "MfgBom")
@Table(name = "mfg_bom")
public class MfgBom extends BaseEntity {

    /**
     * bomCode
     */
    @Column(name = "bom_code", length = 40, nullable = false)
    public String bomCode;

    /**
     * Finished good this BOM produces.
     */
    @Column(name = "item_id", nullable = false)
    public Long itemId;

    /**
     * revision
     */
    @Column(name = "revision", length = 20)
    public String revision;

    /**
     * Output quantity the component lines are based on.
     */
    @Column(name = "quantity", precision = 9, scale = 6)
    public BigDecimal quantity;

    /**
     * uom
     */
    @Column(name = "uom", length = 20)
    public String uom;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public BomStatus status;

    /**
     * A phantom assembly is exploded rather than stocked.
     */
    @Column(name = "is_phantom")
    public Boolean isPhantom;

    /**
     * effectiveFrom
     */
    @Column(name = "effective_from")
    public LocalDate effectiveFrom;

    /**
     * effectiveTo
     */
    @Column(name = "effective_to")
    public LocalDate effectiveTo;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return bomCode;
    }

}
