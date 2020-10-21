package org.monarchinitiative.squirls.autoconfigure.exception;

/**
 * This exception is thrown when a resource is corrupted or if the resource should be present but it is missing.
 * In contrast with {@link UndefinedSquirlsResourceException}, this has nothing to do with the user.
 */
public class InvalidSquirlsResourceException extends Exception {

    public InvalidSquirlsResourceException() {
        super();
    }

    public InvalidSquirlsResourceException(String message) {
        super(message);
    }

    public InvalidSquirlsResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSquirlsResourceException(Throwable cause) {
        super(cause);
    }

    protected InvalidSquirlsResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
