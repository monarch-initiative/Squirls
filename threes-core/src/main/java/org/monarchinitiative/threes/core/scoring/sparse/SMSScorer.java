package org.monarchinitiative.threes.core.scoring.sparse;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.calculators.sms.SMSCalculator;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Optional;
import java.util.function.Function;

/**
 * This scorer
 */
public class SMSScorer implements SplicingScorer {

    /**
     * Since we are working with septamers, the padding is 6bp. The remaining bp is coming from REF/ALT alleles.
     */
    private final int PADDING = 6;

    private final SMSCalculator calculator;

    public SMSScorer(SMSCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public Function<SplicingTernate, Double> scoringFunction() {
        return ter -> {
            GenomeVariant variant = ter.getVariant().withStrand(ter.getRegion().getInterval().getStrand());
            SequenceInterval si = ter.getSequenceInterval();

            GenomeInterval upstream = new GenomeInterval(variant.getGenomeInterval().getGenomeBeginPos().shifted(-PADDING), PADDING);
            GenomeInterval downstream = new GenomeInterval(variant.getGenomeInterval().getGenomeEndPos(), PADDING);
            Optional<String> upstreamSequence = si.getSubsequence(upstream);
            Optional<String> downstreamSequence = si.getSubsequence(downstream);
            String paddedRefAllele = upstreamSequence + variant.getRef() + downstreamSequence;
            String paddedAltAllele = upstreamSequence + variant.getAlt() + downstreamSequence;

            double ref = calculator.scoreSequence(paddedRefAllele);
            double alt = calculator.scoreSequence(paddedAltAllele);
            // subtract total alt from total ref
            // the score should be high if the alt allele abolishes ESE element in ref allele
            return ref - alt;
        };
    }
}
