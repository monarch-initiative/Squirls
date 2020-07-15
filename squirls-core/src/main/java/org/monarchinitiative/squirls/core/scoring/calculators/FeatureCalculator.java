package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

public interface FeatureCalculator {

    /**
     * Calculate feature for given <code>variant</code> against specific <code>transcript</code> using
     * <code>sequence</code>.
     *
     * @param variant    variant we calculate the feature for
     * @param transcript transcript we evaluate the variant against
     * @param sequence   FASTA sequence for the calculation
     * @return feature value
     */
    double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence);
}
