package com.erp.hr;

import java.math.BigDecimal;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Leave type with entitlement rules (M-HR-005). Annual leave accrues per the rule and prorates
 * for joiners and leavers.
 */
@Entity(name = "HrLeaveType")
@Table(name = "hr_leave_type")
public class HrLeaveType extends BaseEntity {

    /**
     * code
     */
    @Column(name = "code", length = 30, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 120, nullable = false)
    public String name;

    /**
     * daysPerYear
     */
    @Column(name = "days_per_year", precision = 9, scale = 6)
    public BigDecimal daysPerYear;

    /**
     * paid
     */
    @Column(name = "paid")
    public Boolean paid;

    /**
     * carryForward
     */
    @Column(name = "carry_forward")
    public Boolean carryForward;

    /**
     * maxCarryForward
     */
    @Column(name = "max_carry_forward", precision = 9, scale = 6)
    public BigDecimal maxCarryForward;

    /**
     * requiresApproval
     */
    @Column(name = "requires_approval")
    public Boolean requiresApproval;

    /**
     * Hex colour used by the leave calendar.
     */
    @Column(name = "colour", length = 20)
    public String colour;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
