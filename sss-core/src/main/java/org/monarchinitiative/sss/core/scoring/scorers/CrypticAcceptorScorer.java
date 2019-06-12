package org.monarchinitiative.sss.core.scoring.scorers;

import org.monarchinitiative.sss.core.model.*;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.allele.AlleleGenerator;

/**
 *
 */
public class CrypticAcceptorScorer implements SplicingScorer {

    private static final int MAX_IN_INTRON = 50;

    private final SplicingInformationContentAnnotator icAnnotator;

    private final AlleleGenerator generator;

    private final int acceptorLength;

    public CrypticAcceptorScorer(SplicingInformationContentAnnotator icAnnotator, AlleleGenerator generator) {
        this.icAnnotator = icAnnotator;
        this.generator = generator;
        this.acceptorLength = icAnnotator.getSplicingParameters().getAcceptorLength();
    }

    @Override
    public double score(SplicingVariant variant, SplicingRegion region, SequenceInterval sequenceInterval) {
        if (!(region instanceof SplicingIntron)) {
            return Double.NaN;
        }
        final SplicingIntron intron = (SplicingIntron) region;
        // this scorer is applied when variant does not overlap with canonical acceptor site. Here we ensure that it really
        // does not overlap the acceptor site
        final AlleleGenerator.Region acceptor = generator.makeAcceptorRegion(intron);
        final GenomeCoordinates varCoor = variant.getCoordinates();
        if (acceptor.overlapsWith(varCoor.getBegin(), varCoor.getEnd())) {
            return Double.NaN;
        }
        final int diff = acceptor.differenceTo(varCoor.getBegin(), varCoor.getEnd());
        if (diff > MAX_IN_INTRON) {
            // we do not score intronic variant that are too deep in intron
            return Double.NaN;
        }

        String altSnippet = sequenceInterval.getSubsequence(varCoor.getBegin() - acceptorLength + 1, varCoor.getBegin())
                + variant.getAlt()
                + sequenceInterval.getSubsequence(varCoor.getEnd(), varCoor.getEnd() + acceptorLength - 1);

        double wtCanonicalAcceptorScore = intron.getAcceptorScore();

        double altCrypticAcceptorScore = SplicingScorer.slidingWindow(altSnippet, acceptorLength)
                .map(icAnnotator::getSpliceAcceptorScore)
                .max(Double::compareTo)
                .orElse(Double.NaN);
        return altCrypticAcceptorScore - wtCanonicalAcceptorScore;
    }
}
