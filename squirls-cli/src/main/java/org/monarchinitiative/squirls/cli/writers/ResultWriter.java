package org.monarchinitiative.squirls.cli.writers;

import java.io.IOException;

/**
 * Implementors write {@link AnalysisResults} in different formats.
 */
public interface ResultWriter {

    void write(AnalysisResults results, OutputSettings outputSettings) throws IOException;

}
