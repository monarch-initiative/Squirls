package org.monarchinitiative.threes.autoconfigure;

/**
 * This exception is thrown when a resource is corrupted or if the resource should be present but it is missing.
 * In contrast with {@link UndefinedThreesResourceException}, this has nothing to do with the user.
 */
public class InvalidThreesResourceException extends Exception {

    public InvalidThreesResourceException() {
        super();
    }

    public InvalidThreesResourceException(String message) {
        super(message);
    }

    public InvalidThreesResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidThreesResourceException(Throwable cause) {
        super(cause);
    }

    protected InvalidThreesResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
