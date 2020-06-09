package org.monarchinitiative.squirls.core.scoring;

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

class CrypticDonorFeatureCalculator extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrypticDonorFeatureCalculator.class);

    /**
     * How many bases we add upstream and downstream when creating snippet for sliding window.
     */
    private final int padding;

    CrypticDonorFeatureCalculator(SplicingInformationContentCalculator calculator, AlleleGenerator generator) {
        super(calculator, generator);
        this.padding = calculator.getSplicingParameters().getDonorLength() - 1;
    }

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        final GenomeInterval donorInterval = generator.makeDonorInterval(anchor);
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // prepare wt donor snippet
        final String donorSnippet;
        if (variantInterval.overlapsWith(donorInterval)) {
            donorSnippet = generator.getDonorSiteWithAltAllele(anchor, variant, sequenceInterval);
        } else {
            donorSnippet = generator.getDonorSiteSnippet(anchor, sequenceInterval);
        }
        if (donorSnippet == null) {
            LOGGER.debug("Unable to create donor snippet at `{}` for variant `{}` using sequence `{}`",
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
        final double wtDonorScore = calculator.getSpliceDonorScore(donorSnippet);
        final Double altMaxScore = Utils.slidingWindow(slidingWindowSnippet, calculator.getSplicingParameters().getDonorLength())
                .map(calculator::getSpliceDonorScore)
                .reduce(Double::max)
                .orElse(0D);

        return altMaxScore - wtDonorScore;
    }
}
