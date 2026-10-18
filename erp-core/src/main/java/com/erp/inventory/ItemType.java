package com.erp.inventory;

/**
 * M-INV-001. Only STOCK items are valued and counted.
 */
public enum ItemType {

    STOCK,
    NON_STOCK,
    SERVICE,
    PHANTOM,
    KIT;

}
