package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

interface FeatureCalculator {

    // TODO - get rid of the anchor, that's specific for calculators that need exon/intron boundary
    double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval);
}
