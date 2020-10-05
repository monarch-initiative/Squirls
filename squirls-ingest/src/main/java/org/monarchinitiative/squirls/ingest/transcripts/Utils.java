package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;

import java.util.Collection;
import java.util.Optional;

class Utils {

    private Utils() {
        // private no-op
    }

    static Optional<GenomeInterval> getGeneBoundariesFromTranscriptModel(Collection<TranscriptModel> transcripts) {
        if (transcripts.isEmpty()) {
            return Optional.empty();
        }
        // prepare interval
        GenomePosition begin = null, end = null;
        for (TranscriptModel tx : transcripts) {
            // inspect begin
            final GenomePosition currentBegin = tx.getTXRegion().getGenomeBeginPos();
            if (begin == null || currentBegin.isLt(begin)) {
                begin = currentBegin;
            }
            // inspect end
            final GenomePosition currentEnd = tx.getTXRegion().getGenomeEndPos();
            if (end == null || currentEnd.isGt(end)) {
                end = currentEnd;
            }
        }

            /*
              we're interested in fetching reference sequence and PhyloP scores for this interval
            */
        return Optional.of(new GenomeInterval(begin, end.differenceTo(begin)));
    }


    static Optional<GenomeInterval> getGeneRegionFromSplicingTranscripts(Collection<SplicingTranscript> transcripts) {
        if (transcripts.isEmpty()) {
            return Optional.empty();
        }
        // prepare interval
        GenomePosition begin = null, end = null;
        for (SplicingTranscript tx : transcripts) {
            // inspect begin
            final GenomePosition currentBegin = tx.getTxRegionCoordinates().getGenomeBeginPos();
            if (begin == null || currentBegin.isLt(begin)) {
                begin = currentBegin;
            }
            // inspect end
            final GenomePosition currentEnd = tx.getTxRegionCoordinates().getGenomeEndPos();
            if (end == null || currentEnd.isGt(end)) {
                end = currentEnd;
            }
        }

            /*
              we're interested in fetching reference sequence and PhyloP scores for this interval
            */
        return Optional.of(new GenomeInterval(begin, end.differenceTo(begin)));
    }
}
