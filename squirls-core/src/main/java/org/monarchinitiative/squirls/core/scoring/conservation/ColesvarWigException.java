package org.monarchinitiative.squirls.core.scoring.conservation;

public class ColesvarWigException extends Exception {
    public ColesvarWigException() {
        super();
    }

    public ColesvarWigException(String message) {
        super(message);
    }

    public ColesvarWigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ColesvarWigException(Throwable cause) {
        super(cause);
    }

    protected ColesvarWigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
