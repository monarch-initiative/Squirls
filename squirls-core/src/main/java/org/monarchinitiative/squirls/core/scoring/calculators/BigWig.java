package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.scoring.Annotatable;
import org.monarchinitiative.squirls.core.scoring.FloatRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param data with variant, transcript and tracks
     * @return score for given <code>variant</code> as described above
     */
    @Override
    public <T extends Annotatable> double score(T data) {
        final FloatRegion phylop = data.getTrack(FeatureCalculator.PHYLOP_TRACK_NAME, FloatRegion.class);
        final float[] scores = phylop.getValuesForInterval(data.getVariant().getGenomeInterval());
        if (scores.length == 0) {
            return Double.NaN;
        } else {
            double sum = 0.;
            for (float score : scores) {
                if (!Float.isNaN(score)) {
                    sum += score;
                }
            }
            return sum / scores.length;
        }
    }
}
