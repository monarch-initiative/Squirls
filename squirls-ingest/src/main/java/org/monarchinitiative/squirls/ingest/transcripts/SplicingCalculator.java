package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;

import java.util.Optional;

public interface SplicingCalculator {

    Optional<SplicingTranscript> calculate(TranscriptModel model);
}
