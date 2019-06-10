package org.monarchinitiative.sss.core.scoring;

import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingIntron;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.AlleleStringGenerator;
import org.monarchinitiative.sss.core.reference.SplicingLocationData;


public class CanonicalDonorScorer implements SpliceScorer {

    private final SplicingInformationContentAnnotator annotator;

    private final SplicingTranscriptLocator locator;

    private final AlleleStringGenerator generator;

    public CanonicalDonorScorer(SplicingInformationContentAnnotator annotator, SplicingTranscriptLocator locator, AlleleStringGenerator generator) {
        this.annotator = annotator;
        this.locator = locator;
        this.generator = generator;
    }

    @Override
    public double score(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {
        final SplicingLocationData location = locator.localize(variant, transcript);
        if (!location.getPosition().equals(SplicingLocationData.SplicingPosition.DONOR)) {
            return Double.NaN;
        }
        final SplicingIntron intron = transcript.getIntrons().get(location.getFeatureIndex());
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
