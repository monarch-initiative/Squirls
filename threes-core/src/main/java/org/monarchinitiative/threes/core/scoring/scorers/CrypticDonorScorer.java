package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.model.*;
import org.monarchinitiative.threes.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;

/**
 *
 */
public class CrypticDonorScorer implements SplicingScorer {

    private static final int MAX_IN_INTRON = -50;

    private final SplicingInformationContentAnnotator icAnnotator;

    private final AlleleGenerator generator;

    private final int donorLength;

    public CrypticDonorScorer(SplicingInformationContentAnnotator icAnnotator, AlleleGenerator generator) {
        this.icAnnotator = icAnnotator;
        this.generator = generator;
        this.donorLength = icAnnotator.getSplicingParameters().getDonorLength();
    }

    @Override
    public double score(SplicingVariant variant, SplicingRegion region, SequenceInterval sequenceInterval) {
        if (!(region instanceof SplicingIntron)) {
            return Double.NaN;
        }
        final SplicingIntron intron = (SplicingIntron) region;
        // this scorer is applied when variant does not overlap with canonical donor site. Here we ensure that it really
        // does not overlap the donor site
        final AlleleGenerator.Region donor = generator.makeDonorRegion(intron);
        final GenomeCoordinates varCoor = variant.getCoordinates();
        if (donor.overlapsWith(varCoor.getBegin(), varCoor.getEnd())) {
            return Double.NaN;
        }

        final int diff = donor.differenceTo(varCoor.getBegin(), varCoor.getEnd());
        if (diff < MAX_IN_INTRON) {
            // we do not score intronic variant that are too deep in intron
            return Double.NaN;
        }

        String altSnippet = sequenceInterval.getSubsequence(varCoor.getBegin() - donorLength + 1, varCoor.getBegin())
                + variant.getAlt()
                + sequenceInterval.getSubsequence(varCoor.getEnd(), varCoor.getEnd() + donorLength - 1);

        double wtCanonicalDonorScore = intron.getDonorScore();

        double altCrypticDonorScore = SplicingScorer.slidingWindow(altSnippet, donorLength)
                .map(icAnnotator::getSpliceDonorScore)
                .max(Double::compareTo)
                .orElse(Double.NaN);
        return altCrypticDonorScore - wtCanonicalDonorScore;
    }
}
