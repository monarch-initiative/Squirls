package org.monarchinitiative.sss.core.scoring;

import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.pwm.SplicingParameters;


public class CanonicalDonorScorer implements SpliceScorer {

    private final SplicingInformationContentAnnotator annotator;

    private final SplicingParameters parameters;

    private final SplicingTranscriptLocator locator;

    public CanonicalDonorScorer(SplicingInformationContentAnnotator annotator, SplicingTranscriptLocator locator) {
        this.annotator = annotator;
        this.parameters = annotator.getSplicingParameters();
        this.locator = locator;
    }

    @Override
    public double score(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {

        return 0;
    }


//    protected Function<SplicingContext, Double> getRawScoringFunction() {
//        return ctx -> {
//            /* if variant region overlaps with donor site, then
//                    create a sequence with incorporated ALT allele
//                    score the sequence
//                    return difference between wt and alt
//               else
//                    return NaN
//            */
//            SplicingContext.SplicePosition pos = ctx.getSplicePosition();
//            switch (pos) {
//                case DONOR:
//                    double wtCanonicalDonorScore = ctx.getTranscript().getExons(ctx.getFeatureIndex()).getDonorScore();
//                    String altCanonicalDonorSnippet = ctx.getDonorSiteWithAltAllele();
//                    if (altCanonicalDonorSnippet == null) {
//                        // e.g. when the whole site is deleted. Other parts of analysis pipeline should interpret such events
//                        return Double.NaN;
//                    }
//                    double altCanonicalDonorScore = icAnnotator.getSpliceDonorScore(altCanonicalDonorSnippet);
//                    return wtCanonicalDonorScore - altCanonicalDonorScore;
//                case INTRON:
//                case ACCEPTOR:
//                case EXON:
//                default:
//                    return Double.NaN;
//            }
//        };
//    }

}
