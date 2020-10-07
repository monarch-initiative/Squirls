package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrypticAcceptor extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalAcceptor.class);

    /**
     * How many bases we add upstream and downstream when creating snippet for sliding window.
     */
    private final int padding;

    public CrypticAcceptor(SplicingInformationContentCalculator calculator,
                           AlleleGenerator generator,
                           SplicingTranscriptLocator locator) {
        super(calculator, generator, locator);
        this.padding = calculator.getSplicingParameters().getAcceptorLength() - 1;
    }

    @Override
    protected double score(GenomeVariant variant, SplicingLocationData locationData, SequenceRegion sequence) {
        return locationData.getAcceptorBoundary()
                .map(anchor -> score(variant, anchor, sequence))
                .orElse(0.);
    }


    private double score(GenomeVariant variant, GenomePosition anchor, SequenceRegion sequence) {
        final GenomeInterval acceptorInterval = generator.makeAcceptorInterval(anchor);
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // prepare wt acceptor snippet
        final String acceptorSnippet;
        if (variantInterval.overlapsWith(acceptorInterval)) {
            acceptorSnippet = generator.getAcceptorSiteWithAltAllele(anchor, variant, sequence);
        } else {
            acceptorSnippet = generator.getAcceptorSiteSnippet(anchor, sequence);
        }
        if (acceptorSnippet == null) {
            LOGGER.debug("Unable to create acceptor snippet at `{}` for variant `{}` using sequence `{}`",
                    anchor, variant, sequence.getInterval());
            return Double.NaN;
        }

        // prepare snippet for sliding window with alt allele
        final String acceptorNeighborSnippet = generator.getAcceptorNeighborSnippet(variantInterval, sequence, variant.getAlt());
        if (acceptorNeighborSnippet == null) {
            LOGGER.debug("Unable to create sliding window snippet +- {}bp for variant `{}` using sequence `{}`",
                    padding, variant, sequence.getInterval());
            return Double.NaN;
        }

        // calculate scores and return result
        final double canonicalAcceptorScore = calculator.getSpliceAcceptorScore(acceptorSnippet);
        final Double crypticMaxScore = Utils.slidingWindow(acceptorNeighborSnippet, calculator.getSplicingParameters().getAcceptorLength())
                .map(calculator::getSpliceAcceptorScore)
                .reduce(Double::max)
                .orElse(0D);

        return crypticMaxScore - canonicalAcceptorScore;
    }
}
