package com.erp.finance;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * House bank account (M-FIN-005). Reconciled against imported statements and used to build the
 * multi-bank cash position report.
 */
@Entity(name = "FinBankAccount")
@Table(name = "fin_bank_account")
public class FinBankAccount extends BaseEntity {

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
     * bankName
     */
    @Column(name = "bank_name", length = 120)
    public String bankName;

    /**
     * accountNumber
     */
    @Column(name = "account_number", length = 60)
    public String accountNumber;

    /**
     * iban
     */
    @Column(name = "iban", length = 40)
    public String iban;

    /**
     * swift
     */
    @Column(name = "swift", length = 20)
    public String swift;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * Control account in the chart of accounts.
     */
    @Column(name = "gl_account_id")
    public Long glAccountId;

    /**
     * defaultAccount
     */
    @Column(name = "default_account")
    public Boolean defaultAccount;

    /**
     * enabled
     */
    @Column(name = "enabled")
    public Boolean enabled;

    @Override
    public String displayLabel() {
        return code + " - " + name;
    }

}
