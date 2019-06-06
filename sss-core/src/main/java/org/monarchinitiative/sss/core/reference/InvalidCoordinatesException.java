package org.monarchinitiative.sss.core.reference;

import org.monarchinitiative.sss.core.ThreeSException;

/**
 *
 */
public class InvalidCoordinatesException extends ThreeSException {

    public InvalidCoordinatesException() {
        super();
    }

    public InvalidCoordinatesException(String message) {
        super(message);
    }

    public InvalidCoordinatesException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCoordinatesException(Throwable cause) {
        super(cause);
    }

    protected InvalidCoordinatesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
