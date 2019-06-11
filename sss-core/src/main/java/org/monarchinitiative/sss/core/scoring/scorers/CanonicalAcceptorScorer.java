package org.monarchinitiative.sss.core.scoring.scorers;

import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingIntron;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.allele.AlleleGenerator;

/**
 *
 */
public class CanonicalAcceptorScorer implements SplicingScorer<SplicingIntron> {

    private final SplicingInformationContentAnnotator annotator;

    private final AlleleGenerator generator;

    public CanonicalAcceptorScorer(SplicingInformationContentAnnotator annotator, AlleleGenerator generator) {
        this.annotator = annotator;
        this.generator = generator;
    }


    @Override
    public double score(SplicingVariant variant, SplicingIntron intron, SequenceInterval sequenceInterval) {
        double wtCanonicalAcceptorScore = intron.getAcceptorScore();
        String altCanonicalAcceptorSnippet = generator.getAcceptorSiteWithAltAllele(intron.getBegin(), variant, sequenceInterval);
        if (altCanonicalAcceptorSnippet == null) {
            // e.g. when the whole site is deleted. Other parts of analysis pipeline should interpret such events
            return Double.NaN;
        }
        double altCanonicalAcceptor = annotator.getSpliceAcceptorScore(altCanonicalAcceptorSnippet);
        return wtCanonicalAcceptorScore - altCanonicalAcceptor;
    }
}
