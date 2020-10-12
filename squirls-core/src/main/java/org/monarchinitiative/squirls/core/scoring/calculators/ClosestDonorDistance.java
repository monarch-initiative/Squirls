package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Comparator;
import java.util.Optional;

/**
 * This class calculates the <code>closest donor</code> feature.
 * <p>
 * A runtime exception is thrown if the class is used to calculate distance to donor site with respect to single-exon
 * transcript.
 */
public class ClosestDonorDistance extends BaseDistanceCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClosestDonorDistance.class);

    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // find the closest donor site
        final Optional<GenomePosition> closestPositionOpt = transcript.getIntrons().stream()
                .map(e -> e.getInterval().getGenomeBeginPos())
                .min(Comparator.comparingInt(border -> Math.abs(border.differenceTo(variantInterval))));

        if (closestPositionOpt.isEmpty()) {
            // this happens only if the transcript has no introns. We should not assess such transcripts in
            // the first place, since there is no splicing there.
            LOGGER.warn("Transcript with 0 introns {} passed here", transcript.getAccessionId());
            return Double.NaN;
        }

        final GenomePosition closestPosition = closestPositionOpt.get();
        return getDiff(variantInterval, closestPosition);
    }
}
