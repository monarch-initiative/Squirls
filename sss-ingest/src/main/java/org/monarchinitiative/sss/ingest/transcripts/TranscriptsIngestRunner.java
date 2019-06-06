package org.monarchinitiative.sss.ingest.transcripts;

import de.charite.compbio.jannovar.data.JannovarData;
import org.monarchinitiative.sss.ingest.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 *
 */
public class TranscriptsIngestRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptsIngestRunner.class);

    private final SplicingCalculator calculator;

    private final TranscriptIngestDao dao;

    private final JannovarData jannovarData;

    public TranscriptsIngestRunner(SplicingCalculator calculator, TranscriptIngestDao dao, JannovarData jannovarData) {
        this.calculator = calculator;
        this.dao = dao;
        this.jannovarData = jannovarData;
    }

    @Override
    public void run() {
        LOGGER.info("Processing {} transcripts", jannovarData.getTmByAccession().values().size());

        ProgressLogger progress = new ProgressLogger();
        int inserted = jannovarData.getTmByAccession().values().parallelStream()
                .peek(progress.logTotal("Processed {} transcripts"))
                .map(calculator::calculate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(dao::insertTranscript)
                .reduce(Integer::sum)
                .orElse(0);
        LOGGER.info("Inserted {} transcripts", inserted);
    }
}
