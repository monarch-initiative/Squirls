package org.monarchinitiative.threes.core.reference.fasta;


import org.monarchinitiative.threes.core.ThreeSRuntimeException;

/**
 *
 */
@Deprecated
public class InvalidCoordinatesException extends ThreeSRuntimeException {

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
