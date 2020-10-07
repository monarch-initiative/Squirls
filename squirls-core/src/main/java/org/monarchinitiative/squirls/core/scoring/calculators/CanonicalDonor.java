package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CanonicalDonor extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalDonor.class);

    public CanonicalDonor(SplicingInformationContentCalculator annotator,
                          AlleleGenerator generator,
                          SplicingTranscriptLocator locator) {
        super(annotator, generator, locator);
    }

    @Override
    protected double score(GenomeVariant variant, SplicingLocationData locationData, SequenceRegion sequence) {
        final Optional<GenomePosition> db = locationData.getDonorBoundary();
        if (db.isEmpty()) {
            return 0.;
        }

        final GenomePosition anchor = db.get();
        final GenomeInterval donorRegion = generator.makeDonorInterval(anchor);

        if (!donorRegion.overlapsWith(variant.getGenomeInterval())) {
            // shortcut - if variant does not affect the donor site
            return 0;
        }

        final String donorSiteSnippet = generator.getDonorSiteSnippet(anchor, sequence);
        final String donorSiteWithAltAllele = generator.getDonorSiteWithAltAllele(anchor, variant, sequence);

        if (donorSiteSnippet == null || donorSiteWithAltAllele == null) {
            LOGGER.debug("Unable to create wt/alt snippets for variant `{}` using interval `{}`", variant, sequence.getInterval());
            return Double.NaN;
        }

        final double refScore = calculator.getSpliceDonorScore(donorSiteSnippet);
        final double altScore = calculator.getSpliceDonorScore(donorSiteWithAltAllele);

        return refScore - altScore;
    }
}
