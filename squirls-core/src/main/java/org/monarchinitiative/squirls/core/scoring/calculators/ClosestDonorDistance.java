package org.monarchinitiative.squirls.core.scoring.calculators;

import com.google.common.collect.ComparisonChain;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import org.monarchinitiative.squirls.core.scoring.Annotatable;

import java.util.Comparator;
import java.util.Optional;

/**
 * This class calculates the <code>closest donor</code> feature.
 * <p>
 * A runtime exception is thrown if the class is used to calculate distance to donor site with respect to single-exon
 * transcript.
 */
public class ClosestDonorDistance extends BaseDistanceCalculator {

    @Override
    public <T extends Annotatable> double score(T data) {
        final GenomeInterval variantInterval = data.getVariant().getGenomeInterval();

        final Comparator<GenomePosition> findClosestExonIntronBorder = (left, right) -> ComparisonChain.start()
                .compare(Math.abs(left.differenceTo(variantInterval)),
                        Math.abs(right.differenceTo(variantInterval)))
                .result();

        // find the closest donor site
        final Optional<GenomePosition> closestPositionOpt = data.getTranscript().getIntrons().stream()
                .map(e -> e.getInterval().getGenomeBeginPos())
                .min(findClosestExonIntronBorder);

        if (closestPositionOpt.isEmpty()) {
            // this happens only if the transcript has no introns. We should not assess such transcripts in
            // the first place, since there is no splicing there.
            throw new RuntimeException("Transcript with 0 introns passed here!");
        }

        final GenomePosition closestPosition = closestPositionOpt.get();
        return getDiff(variantInterval, closestPosition);
    }
}
