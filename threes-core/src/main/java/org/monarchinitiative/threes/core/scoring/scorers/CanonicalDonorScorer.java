package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.*;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;

import java.util.function.Function;


public class CanonicalDonorScorer implements SplicingScorer {

    private final SplicingInformationContentCalculator annotator;

    private final AlleleGenerator generator;

    public CanonicalDonorScorer(SplicingInformationContentCalculator annotator,
                                AlleleGenerator generator) {
        this.annotator = annotator;
        this.generator = generator;
    }


    @Override
    public Function<SplicingTernate, Double> scoringFunction() {
        return t -> {
            final SplicingRegion region = t.getRegion();
            final SequenceInterval sequenceInterval = t.getSequenceInterval();
            final SplicingVariant variant = t.getVariant();

            if (!(region instanceof SplicingIntron)) {
                return Double.NaN;
            }
            final SplicingIntron intron = (SplicingIntron) region;
            double wtCanonicalDonorScore = intron.getDonorScore();
            String altCanonicalDonorSnippet = generator.getDonorSiteWithAltAllele(intron.getBegin(), variant, sequenceInterval);
            if (altCanonicalDonorSnippet == null) {
                // e.g. when the whole site is deleted. Other parts of analysis pipeline should interpret such events
                return Double.NaN;
            }
            double altCanonicalDonorScore = annotator.getSpliceDonorScore(altCanonicalDonorSnippet);
            return wtCanonicalDonorScore - altCanonicalDonorScore;
        };
    }
}
