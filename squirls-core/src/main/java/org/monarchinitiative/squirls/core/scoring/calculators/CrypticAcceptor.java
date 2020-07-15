package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Optional;

public class CrypticAcceptor extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalAcceptor.class);

    /**
     * How many bases we add upstream and downstream when creating snippet for sliding window.
     */
    private final int padding;

    public CrypticAcceptor(SplicingInformationContentCalculator calculator, AlleleGenerator generator) {
        super(calculator, generator);
        this.padding = calculator.getSplicingParameters().getAcceptorLength() - 1;
    }

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        final GenomeInterval acceptorInterval = generator.makeAcceptorInterval(anchor);
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // prepare wt donor snippet
        final String donorSnippet;
        if (variantInterval.overlapsWith(acceptorInterval)) {
            donorSnippet = generator.getAcceptorSiteWithAltAllele(anchor, variant, sequenceInterval);
        } else {
            donorSnippet = generator.getAcceptorSiteSnippet(anchor, sequenceInterval);
        }
        if (donorSnippet == null) {
            LOGGER.debug("Unable to create acceptor snippet at `{}` for variant `{}` using sequence `{}`",
                    anchor, variant, sequenceInterval.getInterval());
            return Double.NaN;
        }

        // prepare snippet for sliding window with alt allele
        final GenomeInterval upstreamPaddingInterval = new GenomeInterval(variantInterval.getGenomeBeginPos().shifted(-padding), padding);
        final Optional<String> upstreamOpt = sequenceInterval.getSubsequence(upstreamPaddingInterval);

        final GenomeInterval downstreamPaddingInterval = new GenomeInterval(variantInterval.getGenomeEndPos(), padding);
        final Optional<String> downstreamOpt = sequenceInterval.getSubsequence(downstreamPaddingInterval);

        if (upstreamOpt.isEmpty() || downstreamOpt.isEmpty()) {
            LOGGER.debug("Unable to create sliding window snippet +- {}bp for variant `{}` using sequence `{}`",
                    padding, variant, sequenceInterval.getInterval());
            return Double.NaN;
        }
        final String slidingWindowSnippet = upstreamOpt.get() + variant.getAlt() + downstreamOpt.get();

        // calculate scores and return result
        final double canonicalAcceptorScore = calculator.getSpliceAcceptorScore(donorSnippet);
        final Double crypticMaxScore = Utils.slidingWindow(slidingWindowSnippet, calculator.getSplicingParameters().getAcceptorLength())
                .map(calculator::getSpliceAcceptorScore)
                .reduce(Double::max)
                .orElse(0D);

        return crypticMaxScore - canonicalAcceptorScore;
    }
}
