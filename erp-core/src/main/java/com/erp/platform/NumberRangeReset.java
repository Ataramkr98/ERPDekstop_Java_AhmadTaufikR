package com.erp.platform;

/**
 * How a number range restarts. NEVER keeps counting up forever, which is what statutory
 * document sequences such as invoice numbering require.
 */
public enum NumberRangeReset {

    NEVER,
    YEARLY,
    MONTHLY;

}
