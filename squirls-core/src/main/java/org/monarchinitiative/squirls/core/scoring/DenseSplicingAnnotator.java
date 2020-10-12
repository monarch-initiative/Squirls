package org.monarchinitiative.squirls.core.scoring;

import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.*;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;

import java.util.Map;

/**
 * This annotator calculates the following splicing features for each {@link Annotatable}:
 * <ul>
 *     <li><code>canonical_donor</code></li>
 *     <li><code>cryptic_donor</code></li>
 *     <li><code>canonical_acceptor</code></li>
 *     <li><code>cryptic_acceptor</code></li>
 *     <li><code>cryptic_acceptor</code></li>
 *     <li><code>hexamer</code></li>
 *     <li><code>septamer</code></li>
 *     <li><code>phylop</code></li>
 *     <li><code>donor_offset</code></li>
 *     <li><code>acceptor_offset</code></li>
 * </ul>
 */
public class DenseSplicingAnnotator extends AbstractSplicingAnnotator {

    /**
     * Create the annotator.
     *
     * @param splicingPwmData splice site data
     * @param hexamerMap      map of hexamer scores from the ESRSeq method
     * @param septamerMap     map of septamer scores from the SMS method
     * @param bigWigAccessor  the accessor that allows to get PhyloP scores from a bigwig file
     */
    public DenseSplicingAnnotator(SplicingPwmData splicingPwmData,
                                  Map<String, Double> hexamerMap,
                                  Map<String, Double> septamerMap,
                                  BigWigAccessor bigWigAccessor) {
        super(new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters()), makeDenseCalculatorMap(splicingPwmData, hexamerMap, septamerMap, bigWigAccessor));
    }

    static Map<String, FeatureCalculator> makeDenseCalculatorMap(SplicingPwmData splicingPwmData,
                                                                 Map<String, Double> hexamerMap,
                                                                 Map<String, Double> septamerMap,
                                                                 BigWigAccessor bigWigAccessor) {

        SplicingTranscriptLocator locator = new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters());
        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        AlleleGenerator generator = new AlleleGenerator(splicingPwmData.getParameters());

        return Map.of(
                "canonical_donor", new CanonicalDonor(calculator, generator, locator),
                "canonical_acceptor", new CanonicalAcceptor(calculator, generator, locator),
                "cryptic_donor", new CrypticDonor(calculator, generator, locator),
                "cryptic_acceptor", new CrypticAcceptor(calculator, generator, locator),
                "hexamer", new Hexamer(hexamerMap),
                "septamer", new Septamer(septamerMap),
                "phylop", new BigWig(bigWigAccessor),
                "donor_offset", new ClosestDonorDistance(),
                "acceptor_offset", new ClosestAcceptorDistance());
    }
}
