package org.monarchinitiative.squirls.autoconfigure.exception;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 *
 */
public class UndefinedSquirlsResourceFailureAnalyzer extends AbstractFailureAnalyzer<UndefinedSquirlsResourceException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, UndefinedSquirlsResourceException cause) {

        return new FailureAnalysis(String.format("Squirls could not be auto-configured properly: '%s'", cause.getMessage()),
                "You need to define all the properties - 'squirls.data-directory', 'squirls.genome-assembly', and 'squirls.data-version'. " +
                        "You can include them in your application.properties or supply your application with '--squirls.data-directory=', etc.. as a startup argument.",
                cause);
    }
}
