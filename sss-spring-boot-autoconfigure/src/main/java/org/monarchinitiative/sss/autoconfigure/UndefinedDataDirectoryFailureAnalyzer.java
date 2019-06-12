package org.monarchinitiative.sss.autoconfigure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class UndefinedDataDirectoryFailureAnalyzer extends AbstractFailureAnalyzer<UndefinedDataDirectoryException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, UndefinedDataDirectoryException cause) {
        return new FailureAnalysis(String.format("Three S could not be auto-configured properly: '%s' is not a valid path", cause), "You need to define a valid path for the Three S data directory.\nTry defining your own threeSDataDirectory bean or include the 'sss.data-directory' property in your application.properties or supply your application with '--sss.data-directory=' as a startup argument.", cause);
    }
}
