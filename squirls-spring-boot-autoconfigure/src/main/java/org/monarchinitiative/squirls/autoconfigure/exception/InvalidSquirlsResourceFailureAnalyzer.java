package org.monarchinitiative.squirls.autoconfigure.exception;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class InvalidSquirlsResourceFailureAnalyzer extends AbstractFailureAnalyzer<InvalidSquirlsResourceException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, InvalidSquirlsResourceException cause) {
        return new FailureAnalysis(String.format("Squirls could not be auto-configured properly: '%s'", cause.getMessage()),
                "This is probably caused by corrupted resources and there is nothing you can do about it. " +
                        "Please contact the developers to get help.",
                cause);
    }
}
