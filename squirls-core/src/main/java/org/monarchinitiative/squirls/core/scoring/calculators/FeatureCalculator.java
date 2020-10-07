package org.monarchinitiative.squirls.core.scoring.calculators;

import org.monarchinitiative.squirls.core.scoring.Annotatable;

public interface FeatureCalculator {

    /**
     * These have to match with whatever is used by {@link org.monarchinitiative.squirls.core.data.SplicingAnnotationData}
     */
    String FASTA_TRACK_NAME = "fasta";
    String PHYLOP_TRACK_NAME = "phylop";

    /**
     * Calculate feature for given <code>variant</code> against specific <code>transcript</code> using
     * <code>sequence</code>.
     *
     * @return feature value
     */
    <T extends Annotatable> double score(T data);

}
