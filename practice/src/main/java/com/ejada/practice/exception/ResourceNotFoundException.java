package com.ejada.practice.exception;

/**
 * Thrown when a requested resource cannot be found.
 *
 * <p>Common scenarios include looking up a user, product, category, or order by
 * an ID that does not exist in the database.  The
 * {@link com.ejada.practice.exception.GlobalExceptionHandler} maps this
 * exception to an HTTP {@code 404 Not Found} response.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ResourceNotFoundException} with the given detail message.
     *
     * @param message human-readable description of what was not found
     *                (e.g. "Product not found with id: 42")
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
