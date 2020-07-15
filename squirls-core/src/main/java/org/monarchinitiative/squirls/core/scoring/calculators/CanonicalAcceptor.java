package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

public class CanonicalAcceptor extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalAcceptor.class);


    public CanonicalAcceptor(SplicingInformationContentCalculator annotator, AlleleGenerator generator) {
        super(annotator, generator);
    }

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval interval) {
        final GenomeInterval acceptorRegion = generator.makeAcceptorInterval(anchor);

        if (!acceptorRegion.overlapsWith(variant.getGenomeInterval())) {
            // shortcut - if variant does not affect the donor site
            return 0;
        }

        final String acceptorSiteSnippet = generator.getAcceptorSiteSnippet(anchor, interval);
        final String acceptorSiteWithAltAllele = generator.getAcceptorSiteWithAltAllele(anchor, variant, interval);

        if (acceptorSiteSnippet == null || acceptorSiteWithAltAllele == null) {
            LOGGER.debug("Unable to create wt/alt snippets for variant `{}` using interval `{}`", variant, interval.getInterval());
            return Double.NaN;
        }

        final double refScore = calculator.getSpliceAcceptorScore(acceptorSiteSnippet);
        final double altScore = calculator.getSpliceAcceptorScore(acceptorSiteWithAltAllele);

        return refScore - altScore;
    }
}
