package com.erp.finance;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Chart of accounts node (M-FIN-001). Hierarchy is unlimited depth via parentId and supports
 * the five standard segments; exportable one line per account.
 */
@Entity(name = "FinAccount")
@Table(name = "fin_account")
public class FinAccount extends BaseEntity {

    /**
     * Account code, e.g. 1100 or 4-1000.
     */
    @Column(name = "code", length = 40, nullable = false)
    public String code;

    /**
     * name
     */
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    /**
     * accountType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 40, nullable = false)
    public AccountType accountType;

    /**
     * Parent node; null for a top-level account.
     */
    @Column(name = "parent_id")
    public Long parentId;

    /**
     * Depth in the tree, maintained on save.
     */
    @Column(name = "level")
    public Integer level;

    /**
     * Group accounts cannot be posted to directly.
     */
    @Column(name = "is_group")
    public Boolean isGroup;

    /**
     * Null means the account accepts any currency.
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * Default tax code for this account (M-FIN-008).
     */
    @Column(name = "tax_code_id")
    public Long taxCodeId;

    /**
     * True for control accounts such as bank or AP that are reconciled to a subledger.
     */
    @Column(name = "reconciliation_account")
    public Boolean reconciliationAccount;

    /**
     * Included in the cash flow statement (M-FIN-011).
     */
    @Column(name = "cash_flow_relevant")
    public Boolean cashFlowRelevant;

    /**
     * description
     */
    @Column(name = "description")
    public String description;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
