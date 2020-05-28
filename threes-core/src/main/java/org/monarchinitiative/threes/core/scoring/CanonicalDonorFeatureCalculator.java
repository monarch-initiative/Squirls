package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.threes.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

class CanonicalDonorFeatureCalculator extends BaseFeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalDonorFeatureCalculator.class);

    CanonicalDonorFeatureCalculator(SplicingInformationContentCalculator annotator, AlleleGenerator generator) {
        super(annotator, generator);
    }

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval interval) {
        final GenomeInterval donorRegion = generator.makeDonorInterval(anchor);

        if (!donorRegion.overlapsWith(variant.getGenomeInterval())) {
            // shortcut - if variant does not affect the donor site
            return 0;
        }

        final String donorSiteSnippet = generator.getDonorSiteSnippet(anchor, interval);
        final String donorSiteWithAltAllele = generator.getDonorSiteWithAltAllele(anchor, variant, interval);

        if (donorSiteSnippet == null || donorSiteWithAltAllele == null) {
            LOGGER.warn("Unable to create wt/alt snippets for variant `{}` using interval `{}`", variant, interval.getInterval());
            return Double.NaN;
        }

        final double refScore = calculator.getSpliceDonorScore(donorSiteSnippet);
        final double altScore = calculator.getSpliceDonorScore(donorSiteWithAltAllele);

        return refScore - altScore;
    }
}