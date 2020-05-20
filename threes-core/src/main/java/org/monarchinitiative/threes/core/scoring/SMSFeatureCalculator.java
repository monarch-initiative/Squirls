package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.scoring.calculators.sms.SMSCalculator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Optional;

/**
 * This scorer TODO - add docs
 */
public class SMSFeatureCalculator implements FeatureCalculator {

    /**
     * Since we are working with septamers, the padding is 6bp. The remaining bp is coming from REF/ALT alleles.
     */
    private final int PADDING = 6;

    private final SMSCalculator calculator;

    public SMSFeatureCalculator(SMSCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        GenomeInterval upstream = new GenomeInterval(variant.getGenomeInterval().getGenomeBeginPos().shifted(-PADDING), PADDING);
        GenomeInterval downstream = new GenomeInterval(variant.getGenomeInterval().getGenomeEndPos(), PADDING);
        Optional<String> upstreamSequence = sequenceInterval.getSubsequence(upstream);
        Optional<String> downstreamSequence = sequenceInterval.getSubsequence(downstream);
        String paddedRefAllele = upstreamSequence + variant.getRef() + downstreamSequence;
        String paddedAltAllele = upstreamSequence + variant.getAlt() + downstreamSequence;

        double ref = calculator.scoreSequence(paddedRefAllele);
        double alt = calculator.scoreSequence(paddedAltAllele);
        // subtract total alt from total ref
        // the score should be high if the alt allele abolishes ESE element in ref allele
        return ref - alt;
    }
}
