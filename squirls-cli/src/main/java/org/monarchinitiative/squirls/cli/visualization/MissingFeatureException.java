package org.monarchinitiative.squirls.cli.visualization;

import org.monarchinitiative.squirls.core.SquirlsException;

public class MissingFeatureException extends SquirlsException {
    public MissingFeatureException() {
        super();
    }

    public MissingFeatureException(String message) {
        super(message);
    }

    public MissingFeatureException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingFeatureException(Throwable cause) {
        super(cause);
    }

    protected MissingFeatureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
