package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;

import java.util.function.Function;

/**
 *
 */
public class CanonicalAcceptorScorer implements SplicingScorer {

    private final SplicingInformationContentCalculator annotator;

    private final AlleleGenerator generator;

    public CanonicalAcceptorScorer(SplicingInformationContentCalculator annotator, AlleleGenerator generator) {
        this.annotator = annotator;
        this.generator = generator;
    }


    @Override
    public Function<SplicingTernate, Double> scoringFunction() {
        return t -> {
            if (!(t.getRegion() instanceof SplicingIntron)) {
                return Double.NaN;
            }
            final SplicingIntron intron = (SplicingIntron) t.getRegion();

            double wtCanonicalAcceptorScore = intron.getAcceptorScore();
            String altCanonicalAcceptorSnippet = generator.getAcceptorSiteWithAltAllele(intron.getEnd(), t.getVariant(), t.getSequenceInterval());
            if (altCanonicalAcceptorSnippet == null) {
                // e.g. when the whole site is deleted. Other parts of analysis pipeline should interpret such events
                return Double.NaN;
            }
            double altCanonicalAcceptor = annotator.getSpliceAcceptorScore(altCanonicalAcceptorSnippet);
            return wtCanonicalAcceptorScore - altCanonicalAcceptor;
        };
    }
}
