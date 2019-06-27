package org.monarchinitiative.threes.core.calculators.ic;


import org.monarchinitiative.threes.core.ThreeSRuntimeException;

/**
 *
 */
public class CorruptedPwmException extends ThreeSRuntimeException {

    public CorruptedPwmException() {
        super();
    }

    public CorruptedPwmException(String message) {
        super(message);
    }

    public CorruptedPwmException(String message, Throwable cause) {
        super(message, cause);
    }

    public CorruptedPwmException(Throwable cause) {
        super(cause);
    }

    protected CorruptedPwmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
