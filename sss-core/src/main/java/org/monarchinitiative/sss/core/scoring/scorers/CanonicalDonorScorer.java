package org.monarchinitiative.sss.core.scoring.scorers;

import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingIntron;
import org.monarchinitiative.sss.core.model.SplicingRegion;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.allele.AlleleGenerator;


public class CanonicalDonorScorer implements SplicingScorer {

    private final SplicingInformationContentAnnotator annotator;

    private final AlleleGenerator generator;

    public CanonicalDonorScorer(SplicingInformationContentAnnotator annotator,
                                AlleleGenerator generator) {
        this.annotator = annotator;
        this.generator = generator;
    }


    @Override
    public double score(SplicingVariant variant, SplicingRegion region, SequenceInterval sequenceInterval) {
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
    }
}
