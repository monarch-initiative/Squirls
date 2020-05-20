package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

public class PhyloPFeatureCalculator implements FeatureCalculator {

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        // TODO - implement
        return 0;
    }
}
