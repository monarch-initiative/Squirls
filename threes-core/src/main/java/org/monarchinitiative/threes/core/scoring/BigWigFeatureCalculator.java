package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.scoring.conservation.BigWigAccessor;
import org.monarchinitiative.threes.core.scoring.conservation.ColesvarWigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.List;

/**
 * This class operates on a bigWig file and gets score for a given {@link GenomeVariant} from a bigWig file.
 */
public class BigWigFeatureCalculator implements FeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigWigFeatureCalculator.class);

    private final BigWigAccessor accessor;

    public BigWigFeatureCalculator(BigWigAccessor accessor) {
        this.accessor = accessor;
    }

    /**
     * Get score for <code>variant</code>'s position from the bigWig file. If the <code>variant</code> is a deletion,
     * then return the mean score. If the score is not available, {@link Double#NaN} is returned.
     *
     * @param anchor           - ignored
     * @param variant          to be annotated
     * @param sequenceInterval - ignored
     * @return score for given <code>variant</code> as described above
     */
    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        try {
            final List<Float> scores = accessor.getScores(variant.getGenomeInterval());
            final double sum = scores.stream()
                    .mapToDouble(Float::doubleValue)
                    .reduce(Double::sum)
                    .orElse(Double.NaN);
            return (sum / scores.size());
        } catch (ColesvarWigException e) {
            LOGGER.warn("Unable to find scores for variant `{}`", variant);
            return Double.NaN;
        }
    }
}
