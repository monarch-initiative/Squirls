package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.squirls.ingest.ProgressLogger;
import org.monarchinitiative.squirls.ingest.dao.TranscriptIngestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class TranscriptsIngestRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptsIngestRunner.class);

    private final SplicingCalculator calculator;

    private final TranscriptIngestDao dao;

    private final Collection<TranscriptModel> transcripts;

    public TranscriptsIngestRunner(SplicingCalculator calculator,
                                   TranscriptIngestDao dao,
                                   Collection<TranscriptModel> transcripts) {
        this.calculator = calculator;
        this.dao = dao;
        this.transcripts = transcripts;
    }

    @Override
    public void run() {
        LOGGER.info("Processing {} transcripts", transcripts.size());

        ProgressLogger progress = new ProgressLogger();
        int inserted = 0;
        final Map<Integer, List<TranscriptModel>> txByChromosome = transcripts.stream()
                .collect(Collectors.groupingBy(TranscriptModel::getChr));
        for (Integer chrom : txByChromosome.keySet()) {
            LOGGER.info("Processing chromosome `{}`", chrom);
            inserted += txByChromosome.get(chrom).parallelStream()
                    .peek(progress.logTotal("Processed {} transcripts"))
                    .map(calculator::calculate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(dao::insertTranscript)
                    .reduce(Integer::sum)
                    .orElse(0);
        }
        LOGGER.info("Inserted {} transcripts", inserted);
    }
}
