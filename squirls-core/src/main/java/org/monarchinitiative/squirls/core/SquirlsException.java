package org.monarchinitiative.squirls.core;

/**
 * Thrown when an error that can be dealt with happens.
 */
public class SquirlsException extends Exception {

    public SquirlsException() {
        super();
    }

    public SquirlsException(String message) {
        super(message);
    }

    public SquirlsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SquirlsException(Throwable cause) {
        super(cause);
    }

    protected SquirlsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
