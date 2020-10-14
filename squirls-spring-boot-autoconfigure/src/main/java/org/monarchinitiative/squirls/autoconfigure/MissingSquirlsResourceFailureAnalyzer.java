package org.monarchinitiative.squirls.autoconfigure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class MissingSquirlsResourceFailureAnalyzer extends AbstractFailureAnalyzer<MissingSquirlsResourceException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, MissingSquirlsResourceException cause) {
        return new FailureAnalysis(String.format("Squirls could not be auto-configured properly: '%s'", cause.getMessage()),
                "This issue would likely be solved by re-downloading and re-creating the SQUIRLS data directory",
                cause);
    }
}
