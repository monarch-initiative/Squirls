package org.monarchinitiative.squirls.cli.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Implementors write {@link AnalysisResults} in different formats.
 */
public interface ResultWriter {

    Logger LOGGER = LoggerFactory.getLogger(ResultWriter.class);

    void write(AnalysisResults results, OutputSettings outputSettings) throws IOException;

}
