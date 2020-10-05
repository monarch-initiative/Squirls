package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.FloatRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.List;

/**
 * This class operates on a bigWig file and gets score for a given {@link GenomeVariant} from a bigWig file.
 */
public class BigWig implements FeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigWig.class);

    public BigWig() {
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
        LOGGER.warn("WARNING - This method is not supposed to be used!");
        return Double.NaN;
    }

    /**
     * Get score for <code>variant</code>'s position from the bigWig file. If the <code>variant</code> is a deletion,
     * then return the mean score. If the score is not available, {@link Double#NaN} is returned.
     *
     * @param variant    to be annotated
     * @param transcript - ignored
     * @param sequence   - ignored
     * @param phylop     - track with PhyloP conservation scores
     * @return score for given <code>variant</code> as described above
     */
    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence, FloatRegion phylop) {
        final List<Float> scores = phylop.getValuesForInterval(variant.getGenomeInterval());
        return scores.stream()
                .mapToDouble(Float::doubleValue)
                .filter(f -> !Double.isNaN(f)) // we ignore the missing values, unless all the values are missing
                .average()
                .orElse(Double.NaN);

    }
}
