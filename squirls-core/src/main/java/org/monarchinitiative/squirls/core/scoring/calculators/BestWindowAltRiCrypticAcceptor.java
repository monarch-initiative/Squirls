package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.scoring.Annotatable;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class calculates <code>alt_ri_acceptor_best_window</code> feature - maximum individual information observed
 * after applying sliding window to the neighboring sequence.
 */
public class BestWindowAltRiCrypticAcceptor implements FeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BestWindowAltRiCrypticAcceptor.class);

    private final SplicingInformationContentCalculator calculator;

    private final AlleleGenerator generator;

    public BestWindowAltRiCrypticAcceptor(SplicingInformationContentCalculator calculator,
                                          AlleleGenerator generator) {
        this.calculator = calculator;
        this.generator = generator;
    }

    /**
     * @param variant  variant we calculate the feature for
     * @param sequence FASTA sequence for the calculation
     * @return feature value
     */

    private double score(GenomeVariant variant, SequenceRegion sequence) {
        final GenomeInterval variantInterval = variant.getGenomeInterval();
        final String acceptorNeighborSnippet = generator.getAcceptorNeighborSnippet(variantInterval, sequence, variant.getAlt());

        if (acceptorNeighborSnippet == null) {
            LOGGER.debug("Unable to create neighborhood snippet for variant `{}` using sequence `{}`", variant, sequence.getInterval());
            return Double.NaN;
        }

        return Utils.slidingWindow(acceptorNeighborSnippet, calculator.getSplicingParameters().getAcceptorLength())
                .map(calculator::getSpliceAcceptorScore)
                .max(Double::compareTo)
                .orElse(Double.NaN);
    }

    @Override
    public <T extends Annotatable> double score(T data) {
        return score(data.getVariant(), data.getTrack(FeatureCalculator.FASTA_TRACK_NAME, SequenceRegion.class));
    }
}
