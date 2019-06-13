package org.monarchinitiative.threes.core;

/**
 *
 */
public class ThreeSException extends Exception {

    public ThreeSException() {
        super();
    }

    public ThreeSException(String message) {
        super(message);
    }

    public ThreeSException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThreeSException(Throwable cause) {
        super(cause);
    }

    protected ThreeSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
