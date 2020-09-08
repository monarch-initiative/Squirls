package org.monarchinitiative.squirls.core.scoring.calculators;

import com.google.common.collect.ComparisonChain;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
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

    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        final Comparator<GenomePosition> findClosestExonIntronBorder = (left, right) -> ComparisonChain.start()
                .compare(Math.abs(left.differenceTo(variantInterval)),
                        Math.abs(right.differenceTo(variantInterval)))
                .result();

        // find the closest donor site
        final Optional<GenomePosition> closestPositionOpt = transcript.getIntrons().stream()
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
