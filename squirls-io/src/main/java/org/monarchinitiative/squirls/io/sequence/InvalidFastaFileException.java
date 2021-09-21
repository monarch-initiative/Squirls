package org.monarchinitiative.squirls.io.sequence;


import org.monarchinitiative.squirls.io.SquirlsResourceException;

/**
 * @author Daniel Danis
 */
public class InvalidFastaFileException extends SquirlsResourceException {

    public InvalidFastaFileException() {
        super();
    }

    public InvalidFastaFileException(String message) {
        super(message);
    }

    public InvalidFastaFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFastaFileException(Throwable cause) {
        super(cause);
    }

    protected InvalidFastaFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
