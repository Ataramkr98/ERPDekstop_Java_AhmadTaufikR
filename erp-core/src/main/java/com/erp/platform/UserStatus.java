package com.erp.platform;

/**
 * Lifecycle of a login. LOCKED is set by the failed-login lockout of M-ADM-001.
 */
public enum UserStatus {

    ACTIVE,
    LOCKED,
    DISABLED;

}
