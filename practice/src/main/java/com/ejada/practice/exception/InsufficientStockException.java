package com.ejada.practice.exception;

/**
 * Thrown when an order cannot be placed because one or more requested products
 * do not have sufficient inventory.
 *
 * <p>The {@link com.ejada.practice.exception.GlobalExceptionHandler} maps this
 * exception to an HTTP {@code 400 Bad Request} response so that the client
 * can inform the end-user of the stock shortage.</p>
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * Constructs a new {@code InsufficientStockException} with the given detail message.
     *
     * @param message human-readable description of the stock shortage
     *                (e.g. "Insufficient stock for product: Widget")
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}
