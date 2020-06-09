package org.monarchinitiative.squirls.core.classifier;

/**
 * An exception thrown when unable to perform prediction.
 */
public class PredictionException extends Exception {

    public PredictionException() {
        super();
    }

    public PredictionException(String message) {
        super(message);
    }

    public PredictionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PredictionException(Throwable cause) {
        super(cause);
    }

    protected PredictionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
