package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * This class calculates <code>alt_ri_donor_best_window</code> feature - maximum individual information observed
 * after applying sliding window to the neighboring sequence.
 */
public class BestWindowAltRiCrypticDonor implements FeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BestWindowAltRiCrypticDonor.class);

    private final SplicingInformationContentCalculator calculator;
    private final AlleleGenerator generator;

    public BestWindowAltRiCrypticDonor(SplicingInformationContentCalculator calculator,
                                       AlleleGenerator generator) {
        this.calculator = calculator;
        this.generator = generator;
    }

    /**
     * @param variant    variant we calculate the feature for
     * @param transcript not used
     * @param sequence   FASTA sequence for the calculation
     * @return feature value
     */
    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final GenomeInterval variantInterval = variant.getGenomeInterval();
        final String donorNeighborSnippet = generator.getDonorNeighborSnippet(variantInterval, sequence, variant.getAlt());

        if (donorNeighborSnippet == null) {
            LOGGER.debug("Unable to create neighborhood snippet for variant `{}` using sequence `{}`", variant, sequence.getInterval());
            return Double.NaN;
        }

        return Utils.slidingWindow(donorNeighborSnippet, calculator.getSplicingParameters().getDonorLength())
                .map(calculator::getSpliceDonorScore)
                .max(Double::compareTo)
                .orElse(Double.NaN);
    }
}
