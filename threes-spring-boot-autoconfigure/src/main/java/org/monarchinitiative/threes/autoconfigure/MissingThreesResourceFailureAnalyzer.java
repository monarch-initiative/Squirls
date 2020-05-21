package org.monarchinitiative.threes.autoconfigure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class MissingThreesResourceFailureAnalyzer extends AbstractFailureAnalyzer<UndefinedThreesResourceException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, UndefinedThreesResourceException cause) {
        return new FailureAnalysis(String.format("3S could not be auto-configured properly: '%s'", cause.getMessage()),
                "This is probably caused by corrupted resources and there is nothing you can do about it. " +
                        "Please contact developers for getting more information.",
                cause);
    }
}
