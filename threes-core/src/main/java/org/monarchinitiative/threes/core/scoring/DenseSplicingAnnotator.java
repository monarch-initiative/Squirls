package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.threes.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.threes.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.scoring.conservation.BigWigAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;
import java.util.Optional;


public class DenseSplicingAnnotator implements SplicingAnnotator {

    private final SplicingTranscriptLocator transcriptLocator;

    // TODO - make a map of FeatureCalculators

    private final CanonicalDonorFeatureCalculator canonicalDonorFeatureCalculator;

    private final CanonicalAcceptorFeatureCalculator canonicalAcceptorScorer;

    private final CrypticDonorFeatureCalculator crypticDonorScorer;

    private final CrypticAcceptorFeatureCalculator crypticAcceptorScorer;

    private final HexamerFeatureCalculator hexamerFeatureCalculator;

    private final SeptamerFeatureCalculator septamerFeatureCalculator;

    private final BigWigFeatureCalculator phyloPFeatureCalculator;

    public DenseSplicingAnnotator(SplicingPwmData splicingPwmData,
                                  Map<String, Double> hexamerMap,
                                  Map<String, Double> septamerMap,
                                  BigWigAccessor bigWigAccessor) {
        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        AlleleGenerator generator = new AlleleGenerator(calculator.getSplicingParameters());

        transcriptLocator = new NaiveSplicingTranscriptLocator(calculator.getSplicingParameters());

        canonicalDonorFeatureCalculator = new CanonicalDonorFeatureCalculator(calculator, generator);
        canonicalAcceptorScorer = new CanonicalAcceptorFeatureCalculator(calculator, generator);
        crypticDonorScorer = new CrypticDonorFeatureCalculator(calculator, generator);
        crypticAcceptorScorer = new CrypticAcceptorFeatureCalculator(calculator, generator);

        hexamerFeatureCalculator = new HexamerFeatureCalculator(hexamerMap);
        septamerFeatureCalculator = new SeptamerFeatureCalculator(septamerMap);

        phyloPFeatureCalculator = new BigWigFeatureCalculator(bigWigAccessor);
    }

    @Override
    public FeatureData evaluate(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {

        final GenomeVariant variantOnStrand = variant.withStrand(transcript.getStrand());
        final SplicingLocationData locationData = transcriptLocator.locate(variant, transcript);

        final Optional<GenomePosition> donorBoundary = locationData.getDonorBoundary();
        final Optional<GenomePosition> acceptorBoundary = locationData.getAcceptorBoundary();

        final FeatureData.Builder builder = FeatureData.builder();

        if (donorBoundary.isPresent()) {
            // `canonical_donor` feature
            final double canonicalDonor = canonicalDonorFeatureCalculator.score(donorBoundary.get(), variantOnStrand, sequenceInterval);
            builder.addFeature("canonical_donor", canonicalDonor);

            // `cryptic_donor` feature
            final double crypticDonor = crypticDonorScorer.score(donorBoundary.get(), variantOnStrand, sequenceInterval);
            builder.addFeature("cryptic_donor", crypticDonor);
        } else {
            builder.addFeature("canonical_donor", 0.);
            builder.addFeature("cryptic_donor", 0.);
        }

        if (acceptorBoundary.isPresent()) {
            // `canonical_acceptor` feature
            final double canonicalAcceptor = canonicalAcceptorScorer.score(acceptorBoundary.get(), variantOnStrand, sequenceInterval);
            builder.addFeature("canonical_acceptor", canonicalAcceptor);

            // `cryptic_acceptor` feature
            final double crypticAcceptor = crypticAcceptorScorer.score(acceptorBoundary.get(), variantOnStrand, sequenceInterval);
            builder.addFeature("cryptic_acceptor", crypticAcceptor);
        } else {
            builder.addFeature("canonical_acceptor", 0.);
            builder.addFeature("cryptic_acceptor", 0.);
        }

        final double hexamerScore = hexamerFeatureCalculator.score(null, variant, sequenceInterval);
        builder.addFeature("hexamer", hexamerScore);
        final double septamerScore = septamerFeatureCalculator.score(null, variant, sequenceInterval);
        builder.addFeature("septamer", septamerScore);

        final double phylopScore = phyloPFeatureCalculator.score(null, variant, sequenceInterval);
        builder.addFeature("phylop", phylopScore);

        // TODO - add other features - donor_offset, acceptor_offset

        return builder.build();

    }
}
