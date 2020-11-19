package org.monarchinitiative.squirls.cli.cmd;

public class SquirlsCommandException extends Exception {

    public SquirlsCommandException() {
        super();
    }

    public SquirlsCommandException(String message) {
        super(message);
    }

    public SquirlsCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public SquirlsCommandException(Throwable cause) {
        super(cause);
    }

    protected SquirlsCommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
