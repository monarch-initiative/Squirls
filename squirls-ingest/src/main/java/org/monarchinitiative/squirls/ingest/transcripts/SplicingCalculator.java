package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface SplicingCalculator {

    Logger LOGGER = LoggerFactory.getLogger(SplicingCalculator.class);

    Optional<SplicingTranscript> calculate(TranscriptModel model);
}
