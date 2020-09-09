package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;

abstract class BaseAgezCalculator implements FeatureCalculator {


    protected final SplicingTranscriptLocator locator;
    protected final int agezBegin;
    protected final int agezEnd;

    BaseAgezCalculator(SplicingTranscriptLocator locator, int agezBegin, int agezEnd) {
        this.locator = locator;
        this.agezBegin = agezBegin;
        this.agezEnd = agezEnd;
    }

    boolean overlapsWithAgezRegion(GenomeVariant variant, SplicingTranscript transcript) {
        final SplicingLocationData locationData = locator.locate(variant, transcript);

        if (locationData.getAcceptorBoundary().isEmpty()) {
            // no acceptor boundary, the variant is located within the coding region or canonical donor region
            // of the first exon
            return false;
        }

        final GenomePosition acceptorBoundary = locationData.getAcceptorBoundary().get();
        final GenomeInterval agezInterval = new GenomeInterval(acceptorBoundary.shifted(agezBegin), -(agezBegin - agezEnd));

        return variant.getGenomeInterval().overlapsWith(agezInterval);
    }
}
