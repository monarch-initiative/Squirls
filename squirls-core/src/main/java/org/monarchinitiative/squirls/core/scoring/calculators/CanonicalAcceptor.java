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

public class CanonicalAcceptor extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalAcceptor.class);


    public CanonicalAcceptor(SplicingInformationContentCalculator annotator,
                             AlleleGenerator generator,
                             SplicingTranscriptLocator locator) {
        super(annotator, generator, locator);
    }

    @Override
    protected double score(GenomeVariant variant, SplicingLocationData locationData, SequenceRegion sequence) {
        final Optional<GenomePosition> ab = locationData.getAcceptorBoundary();
        if (ab.isEmpty()) {
            return 0.;
        }
        final GenomePosition anchor = ab.get();
        final GenomeInterval acceptorRegion = generator.makeAcceptorInterval(anchor);

        if (!acceptorRegion.overlapsWith(variant.getGenomeInterval())) {
            // shortcut - if variant does not affect the donor site
            return 0;
        }

        final String acceptorSiteSnippet = generator.getAcceptorSiteSnippet(anchor, sequence);
        final String acceptorSiteWithAltAllele = generator.getAcceptorSiteWithAltAllele(anchor, variant, sequence);

        if (acceptorSiteSnippet == null || acceptorSiteWithAltAllele == null) {
            LOGGER.debug("Unable to create wt/alt snippets for variant `{}` using interval `{}`", variant, sequence.getInterval());
            return Double.NaN;
        }

        final double refScore = calculator.getSpliceAcceptorScore(acceptorSiteSnippet);
        final double altScore = calculator.getSpliceAcceptorScore(acceptorSiteWithAltAllele);

        return refScore - altScore;
    }
}
