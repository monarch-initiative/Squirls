package org.monarchinitiative.threes.core.scoring.sparse;

import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 *
 */
public class CanonicalAcceptorScorer implements SplicingScorer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalAcceptorScorer.class);

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
                LOGGER.warn("Did not receive an intron");
                return Double.NaN;
            }
            final SplicingIntron intron = (SplicingIntron) t.getRegion();

            double wtCanonicalAcceptorScore = intron.getAcceptorScore();
            String altCanonicalAcceptorSnippet = generator.getAcceptorSiteWithAltAllele(intron.getInterval().getGenomeEndPos(), t.getVariant(), t.getSequenceInterval());
            if (altCanonicalAcceptorSnippet == null) {
                // e.g. when the whole site is deleted. Other parts of analysis pipeline should interpret such events
                return Double.NaN;
            }
            double altCanonicalAcceptor = annotator.getSpliceAcceptorScore(altCanonicalAcceptorSnippet);
            return wtCanonicalAcceptorScore - altCanonicalAcceptor;
        };
    }
}
