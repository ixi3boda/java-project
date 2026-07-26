package com.ejada.practice.exception;

/**
 * Thrown when an attempt is made to create a resource that already exists.
 *
 * <p>Examples include registering a username or e-mail address that is already
 * in use, or creating a category whose name clashes with an existing one.
 * The {@link com.ejada.practice.exception.GlobalExceptionHandler} maps this
 * exception to an HTTP {@code 409 Conflict} response.</p>
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs a new {@code DuplicateResourceException} with the given detail message.
     *
     * @param message human-readable description of the conflict (e.g. "Username already taken: bob")
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
