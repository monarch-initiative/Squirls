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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 *     <li><code>wt_ri_donor</code></li>
 *     <li><code>wt_ri_acceptor</code></li>
 *     <li><code>alt_ri_best_window_donor</code></li>
 *     <li><code>alt_ri_best_window_acceptor</code></li>
 *     <li><code>s_strength_diff_donor</code></li>
 *     <li><code>s_strength_diff_acceptor</code></li>
 *     <li><code>exon_length</code></li>
 *     <li><code>intron_length</code></li>
 * </ul>
 * The {@link de.charite.compbio.jannovar.reference.GenomePosition}s of the closest donor and acceptor sites.
 * <p>
 * Note that the features are computed only if the transcript has at least a single intron.
 */
public class RichSplicingAnnotator implements SplicingAnnotator {

    private final SplicingTranscriptLocator locator;

    private final Map<String, FeatureCalculator> calculatorMap;


    public RichSplicingAnnotator(SplicingPwmData splicingPwmData,
                                 Map<String, Double> hexamerMap,
                                 Map<String, Double> septamerMap,
                                 BigWigAccessor bigWigAccessor) {
        locator = new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters());

        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        AlleleGenerator generator = new AlleleGenerator(splicingPwmData.getParameters());

        Map<String, FeatureCalculator> denseCalculators = Map.of(
                "canonical_donor", new CanonicalDonor(calculator, generator, locator),
                "canonical_acceptor", new CanonicalAcceptor(calculator, generator, locator),
                "cryptic_donor", new CrypticDonor(calculator, generator, locator),
                "cryptic_acceptor", new CrypticAcceptor(calculator, generator, locator),
                "hexamer", new Hexamer(hexamerMap),
                "septamer", new Septamer(septamerMap),
                "phylop", new BigWig(bigWigAccessor),
                "donor_offset", new ClosestDonorDistance(),
                "acceptor_offset", new ClosestAcceptorDistance());

        Map<String, FeatureCalculator> richCalculators = Map.of(
                "wt_ri_donor", new WtRiDonor(calculator, generator, locator),
                "wt_ri_acceptor", new WtRiAcceptor(calculator, generator, locator),
                "alt_ri_best_window_donor", new BestWindowAltRiCrypticDonor(calculator, generator),
                "alt_ri_best_window_acceptor", new BestWindowAltRiCrypticAcceptor(calculator, generator),
                "s_strength_diff_donor", new SStrengthDiffDonor(calculator, generator, locator),
                "s_strength_diff_acceptor", new SStrengthDiffAcceptor(calculator, generator, locator),
                "exon_length", new ExonLength(locator),
                "intron_length", new IntronLength(locator));

        calculatorMap = Stream.concat(denseCalculators.entrySet().stream(), richCalculators.entrySet().stream())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public <T extends Annotatable> T annotate(T data) {
        final GenomeVariant variant = data.getVariant();
        final SplicingTranscript transcript = data.getTranscript();
        final SequenceInterval sequence = data.getSequence();

        if (transcript.getIntrons().isEmpty()) {
            // no splicing, no annotations
            return data;
        }

        final SplicingLocationData locationData = locator.locate(variant, transcript);
        final GenomeVariant variantOnStrand = variant.withStrand(transcript.getStrand());

        // calculate the features
        calculatorMap.forEach((name, calculator) -> data.putFeature(name, calculator.score(variantOnStrand, transcript, sequence)));

        final Metadata.Builder metadataBuilder = Metadata.builder();
        locationData.getDonorBoundary().ifPresent(boundary -> metadataBuilder.putDonorCoordinate(transcript.getAccessionId(), boundary));
        locationData.getAcceptorBoundary().ifPresent(boundary -> metadataBuilder.putAcceptorCoordinate(transcript.getAccessionId(), boundary));

        data.setMetadata(metadataBuilder.build());

        return data;
    }
}
