package com.ejada.practice.entity;

/**
 * Enumeration of all possible lifecycle states for an {@link Order}.
 *
 * <p>The typical happy-path progression is:
 * <pre>
 *   PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 * </pre>
 * An order may also be {@link #CANCELLED} at any pre-shipment stage,
 * or {@link #REFUNDED} after delivery.</p>
 */
public enum OrderStatus {

    /**
     * The order has been submitted by the customer but not yet reviewed.
     */
    PENDING,

    /**
     * The order has been accepted and payment confirmed.
     */
    CONFIRMED,

    /**
     * The order is being prepared / packed in the warehouse.
     */
    PROCESSING,

    /**
     * The order has left the warehouse and is in transit.
     */
    SHIPPED,

    /**
     * The order has been successfully delivered to the customer.
     */
    DELIVERED,

    /**
     * The order was cancelled before shipment.
     */
    CANCELLED,

    /**
     * The delivered order was returned and a refund has been issued.
     */
    REFUNDED
}
