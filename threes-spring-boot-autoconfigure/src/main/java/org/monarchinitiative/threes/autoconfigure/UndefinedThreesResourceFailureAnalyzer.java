package org.monarchinitiative.threes.autoconfigure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 *
 */
public class UndefinedThreesResourceFailureAnalyzer extends AbstractFailureAnalyzer<UndefinedThreesResourceException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, UndefinedThreesResourceException cause) {

        return new FailureAnalysis(String.format("3S could not be auto-configured properly: '%s'", cause.getMessage()),
                "You need to define all the properties - 'threes.data-directory', 'threes.genome-assembly', 'threes.data-version', and 'threes.transcript-source'." +
                        "You can include them in your application.properties or supply your application with '--threes.data-directory=', etc.. as a startup argument.",
                cause);
    }
}
