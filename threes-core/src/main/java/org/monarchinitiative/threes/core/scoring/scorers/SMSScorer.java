package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.calculators.sms.SMSCalculator;
import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.model.SplicingVariant;

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
            SplicingVariant variant = ter.getVariant();
            SequenceInterval si = ter.getSequenceInterval();
            int varBegin = variant.getCoordinates().getBegin();
            int varEnd = variant.getCoordinates().getEnd();

            String upstream = si.getSubsequence(varBegin - PADDING, varBegin);
            String downstream = si.getSubsequence(varEnd, varEnd + PADDING);
            String refAllele = upstream + variant.getRef() + downstream;
            String altAllele = upstream + variant.getAlt() + downstream;

            double ref = calculator.scoreSequence(refAllele);
            double alt = calculator.scoreSequence(altAllele);
            // total ref from total alt
            return alt - ref;
        };
    }
}
