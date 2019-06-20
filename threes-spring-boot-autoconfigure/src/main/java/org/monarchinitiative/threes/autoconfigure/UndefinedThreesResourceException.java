package org.monarchinitiative.threes.autoconfigure;

/**
 *
 */
public class UndefinedThreesResourceException extends Exception {

    public UndefinedThreesResourceException() {
        super();
    }

    public UndefinedThreesResourceException(String message) {
        super(message);
    }

    public UndefinedThreesResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UndefinedThreesResourceException(Throwable cause) {
        super(cause);
    }

    protected UndefinedThreesResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
