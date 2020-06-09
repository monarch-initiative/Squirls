package org.monarchinitiative.squirls.autoconfigure;

/**
 * This exception is thrown during auto-configuration, if an information that should have been provided by the user
 * is missing.
 */
public class UndefinedSquirlsResourceException extends Exception {

    public UndefinedSquirlsResourceException() {
        super();
    }

    public UndefinedSquirlsResourceException(String message) {
        super(message);
    }

    public UndefinedSquirlsResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UndefinedSquirlsResourceException(Throwable cause) {
        super(cause);
    }

    protected UndefinedSquirlsResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
