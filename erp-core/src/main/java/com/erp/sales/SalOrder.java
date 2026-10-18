package com.erp.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Sales order header (M-SAL-003). Saving a confirmed order creates a soft reservation against
 * available-to-promise stock (M-SAL-007).
 */
@Entity(name = "SalOrder")
@Table(name = "sal_order")
public class SalOrder extends BaseEntity {

    /**
     * orderNumber
     */
    @Column(name = "order_number", length = 40, nullable = false)
    public String orderNumber;

    /**
     * customerId
     */
    @Column(name = "customer_id", nullable = false)
    public Long customerId;

    /**
     * orderDate
     */
    @Column(name = "order_date", nullable = false)
    public LocalDate orderDate;

    /**
     * requestedDate
     */
    @Column(name = "requested_date")
    public LocalDate requestedDate;

    /**
     * promisedDate
     */
    @Column(name = "promised_date")
    public LocalDate promisedDate;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public SalesOrderStatus status;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * exchangeRate
     */
    @Column(name = "exchange_rate", precision = 9, scale = 6)
    public BigDecimal exchangeRate;

    /**
     * subtotal
     */
    @Column(name = "subtotal", precision = 19, scale = 4)
    public BigDecimal subtotal;

    /**
     * discountAmount
     */
    @Column(name = "discount_amount", precision = 19, scale = 4)
    public BigDecimal discountAmount;

    /**
     * taxAmount
     */
    @Column(name = "tax_amount", precision = 19, scale = 4)
    public BigDecimal taxAmount;

    /**
     * totalAmount
     */
    @Column(name = "total_amount", precision = 19, scale = 4)
    public BigDecimal totalAmount;

    /**
     * warehouseId
     */
    @Column(name = "warehouse_id")
    public Long warehouseId;

    /**
     * salespersonId
     */
    @Column(name = "salesperson_id")
    public Long salespersonId;

    /**
     * quotationId
     */
    @Column(name = "quotation_id")
    public Long quotationId;

    /**
     * priceListId
     */
    @Column(name = "price_list_id")
    public Long priceListId;

    /**
     * shippingAddress
     */
    @Column(name = "shipping_address", length = 400)
    public String shippingAddress;

    /**
     * Customer's own PO number.
     */
    @Column(name = "customer_reference", length = 120)
    public String customerReference;

    /**
     * creditChecked
     */
    @Column(name = "credit_checked")
    public Boolean creditChecked;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return orderNumber;
    }

}
