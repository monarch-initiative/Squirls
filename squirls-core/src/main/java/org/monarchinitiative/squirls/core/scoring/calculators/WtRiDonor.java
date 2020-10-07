package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class calculates <code>wt_ri_donor</code> feature - individual information of the <em>wt/ref</em> allele of the
 * donor site.
 */
public class WtRiDonor extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WtRiDonor.class);

    public WtRiDonor(SplicingInformationContentCalculator calculator,
                     AlleleGenerator generator,
                     SplicingTranscriptLocator locator) {
        super(calculator, generator, locator);
    }

    @Override
    protected double score(GenomeVariant variant, SplicingLocationData locationData, SequenceRegion sequence) {
        return locationData.getDonorBoundary()
                .map(anchor -> score(variant, anchor, sequence))
                .orElse(0.);
    }


    private double score(GenomeVariant variant, GenomePosition anchor, SequenceRegion sequence) {
        final String donorSiteSnippet = generator.getDonorSiteSnippet(anchor, sequence);

        if (donorSiteSnippet == null) {
            LOGGER.debug("Unable to create wt/alt snippets for variant `{}` using sequence `{}`", variant, sequence.getInterval());
            return Double.NaN;
        }

        return calculator.getSpliceDonorScore(donorSiteSnippet);
    }
}
