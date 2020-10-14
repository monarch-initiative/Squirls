package org.monarchinitiative.squirls.autoconfigure.exception;

/**
 * An exception thrown when a resource file (e.g. FASTA file) is missing from SQUIRLS data directory.
 */
public class MissingSquirlsResourceException extends Exception {


    public MissingSquirlsResourceException() {
        super();
    }

    public MissingSquirlsResourceException(String message) {
        super(message);
    }

    public MissingSquirlsResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingSquirlsResourceException(Throwable cause) {
        super(cause);
    }

    protected MissingSquirlsResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
