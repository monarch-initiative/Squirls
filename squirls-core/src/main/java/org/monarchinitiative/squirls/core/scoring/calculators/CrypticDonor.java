package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

public class CrypticDonor extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrypticDonor.class);

    /**
     * How many bases we add upstream and downstream when creating snippet for sliding window.
     */
    private final int padding;

    public CrypticDonor(SplicingInformationContentCalculator calculator,
                        AlleleGenerator generator,
                        SplicingTranscriptLocator locator) {
        super(calculator, generator, locator);
        this.padding = calculator.getSplicingParameters().getDonorLength() - 1;
    }

    @Override
    protected double score(GenomeVariant variant, SplicingLocationData locationData, SequenceInterval sequence) {
        return locationData.getDonorBoundary()
                .map(anchor -> score(variant, anchor, sequence))
                .orElse(0.);
    }


    private double score(GenomeVariant variant, GenomePosition anchor, SequenceInterval sequence) {
        final GenomeInterval donorInterval = generator.makeDonorInterval(anchor);
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // prepare wt donor snippet
        final String donorSnippet;
        if (variantInterval.overlapsWith(donorInterval)) {
            donorSnippet = generator.getDonorSiteWithAltAllele(anchor, variant, sequence);
        } else {
            donorSnippet = generator.getDonorSiteSnippet(anchor, sequence);
        }
        if (donorSnippet == null) {
            LOGGER.debug("Unable to create donor snippet at `{}` for variant `{}` using sequence `{}`",
                    anchor, variant, sequence.getInterval());
            return Double.NaN;
        }

        // prepare snippet for sliding window with alt allele
        final String donorNeighborSnippet = generator.getDonorNeighborSnippet(variantInterval, sequence, variant.getAlt());
        if (donorNeighborSnippet == null) {
            LOGGER.debug("Unable to create sliding window snippet +- {}bp for variant `{}` using sequence `{}`",
                    padding, variant, sequence.getInterval());
            return Double.NaN;
        }

        // calculate scores and return result
        final double canonicalDonorScore = calculator.getSpliceDonorScore(donorSnippet);
        final Double crypticMaxScore = Utils.slidingWindow(donorNeighborSnippet, calculator.getSplicingParameters().getDonorLength())
                .map(calculator::getSpliceDonorScore)
                .reduce(Double::max)
                .orElse(0D);

        return crypticMaxScore - canonicalDonorScore;
    }
}
