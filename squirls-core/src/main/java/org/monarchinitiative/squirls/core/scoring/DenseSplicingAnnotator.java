package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Metadata;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.*;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

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
public class DenseSplicingAnnotator implements SplicingAnnotator {

    private final SplicingTranscriptLocator locator;

    /**
     * Mapping from feature name to feature calculator.
     */
    private final Map<String, FeatureCalculator> calculatorMap;

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

        locator = new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters());

        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        AlleleGenerator generator = new AlleleGenerator(splicingPwmData.getParameters());

        calculatorMap = Map.of(
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

    @Override
    public <T extends Annotatable> T annotate(T data) {
        final GenomeVariant variant = data.getVariant();
        final SplicingTranscript transcript = data.getTranscript();
        final SequenceInterval sequence = data.getSequence();

        final SplicingLocationData locationData = locator.locate(variant, transcript);
        final GenomeVariant variantOnStrand = variant.withStrand(transcript.getStrand());

        // all features except for the offsets
        calculatorMap.forEach((name, calculator) -> data.putFeature(name, calculator.score(variantOnStrand, transcript, sequence)));

        final Metadata.Builder metadataBuilder = Metadata.builder();

        locationData.getDonorBoundary().ifPresent(boundary -> metadataBuilder.putDonorCoordinate(transcript.getAccessionId(), boundary));
        locationData.getAcceptorBoundary().ifPresent(boundary -> metadataBuilder.putAcceptorCoordinate(transcript.getAccessionId(), boundary));

        data.setMetadata(metadataBuilder.build());

        return data;
    }
}
