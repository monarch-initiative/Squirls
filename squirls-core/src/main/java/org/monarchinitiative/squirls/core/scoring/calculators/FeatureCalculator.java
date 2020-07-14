package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

interface FeatureCalculator {

    double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval);
}
