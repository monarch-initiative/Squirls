package org.monarchinitiative.threes.autoconfigure;

/**
 * This exception is thrown when a resource that should be present is missing. However, in contrast with
 * {@link UndefinedThreesResourceException}, this has nothing to do with the user.
 */
public class CorruptedThreesResourceException extends Exception {

    public CorruptedThreesResourceException() {
        super();
    }

    public CorruptedThreesResourceException(String message) {
        super(message);
    }

    public CorruptedThreesResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CorruptedThreesResourceException(Throwable cause) {
        super(cause);
    }

    protected CorruptedThreesResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
