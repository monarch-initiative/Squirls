package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.ColesvarWigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.List;

/**
 * This class operates on a bigWig file and gets score for a given {@link GenomeVariant} from a bigWig file.
 */
public class BigWig implements FeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigWig.class);

    private final BigWigAccessor accessor;

    public BigWig(BigWigAccessor accessor) {
        this.accessor = accessor;
    }

    /**
     * Get score for <code>variant</code>'s position from the bigWig file. If the <code>variant</code> is a deletion,
     * then return the mean score. If the score is not available, {@link Double#NaN} is returned.
     *
     * @param variant    to be annotated
     * @param transcript - ignored
     * @param sequence   - ignored
     * @return score for given <code>variant</code> as described above
     */
    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        try {
            final List<Float> scores = accessor.getScores(variant.getGenomeInterval());
            final double sum = scores.stream()
                    .mapToDouble(Float::doubleValue)
                    .reduce(Double::sum)
                    .orElse(Double.NaN);
            return (sum / scores.size());
        } catch (ColesvarWigException e) {
            LOGGER.debug("Unable to find scores for variant `{}`", variant);
            return Double.NaN;
        }
    }
}
