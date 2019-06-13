package org.monarchinitiative.threes.core;

/**
 *
 */
public class ThreeSRuntimeException extends RuntimeException {

    public ThreeSRuntimeException() {
        super();
    }

    public ThreeSRuntimeException(String message) {
        super(message);
    }

    public ThreeSRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThreeSRuntimeException(Throwable cause) {
        super(cause);
    }

    protected ThreeSRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
