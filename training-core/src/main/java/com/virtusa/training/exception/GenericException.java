package com.virtusa.training.exception;

/**
 * The Class GenericException.
 */
public class GenericException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new application exception.
     *
     * @param message the message
     */
    public GenericException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new application exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public GenericException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new application exception.
     *
     * @param cause the cause
     */
    public GenericException(final Throwable cause) {
        super(cause);
    }

}
