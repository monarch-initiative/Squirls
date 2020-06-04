package org.monarchinitiative.threes.core.scoring;

import com.google.common.collect.ComparisonChain;
import de.charite.compbio.jannovar.reference.GenomeInterval;
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

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


public class DenseSplicingAnnotator implements SplicingAnnotator {

    private final SplicingTranscriptLocator transcriptLocator;

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

    static int getOffset(Stream<GenomePosition> positions, GenomeInterval variant) {
        final Comparator<GenomePosition> findClosestExonIntronBorder = (left, right) -> ComparisonChain.start()
                .compare(Math.abs(left.differenceTo(variant)), Math.abs(right.differenceTo(variant)))
                .result();

        final GenomePosition closestPosition = positions.min(findClosestExonIntronBorder).orElseThrow();
        final int diff = closestPosition.differenceTo(variant);
        if (diff > 0) {
            // variant is upstream from the border position
            return -diff;
        } else if (diff < 0) {
            // variant is downstream from the border position
            return -diff + 1;
        } else {
            /*
            Due to representation of exon|Intron / intron|Exon boundary as a GenomePosition that represents position of
            the capital E/I character above, we need to distinguish when variant interval denotes
              - a deletion of the boundary, or
              - SNP at +1 position.

            The code below handles these situations.
            */
            if (variant.contains(closestPosition) && variant.length() > 1) {
                // deletion of the boundary
                return 0;
            } else {
                // SNP at +1 position
                return 1;
            }
        }
    }

    @Override
    public SplicingAnnotationData evaluate(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {

        final GenomeVariant variantOnStrand = variant.withStrand(transcript.getStrand());
        final SplicingLocationData locationData = transcriptLocator.locate(variant, transcript);

        final Optional<GenomePosition> donorBoundary = locationData.getDonorBoundary();
        final Optional<GenomePosition> acceptorBoundary = locationData.getAcceptorBoundary();

        final FeatureData.Builder featureData = FeatureData.builder();

        if (donorBoundary.isPresent()) {
            // `canonical_donor` feature
            final double canonicalDonor = canonicalDonorFeatureCalculator.score(donorBoundary.get(), variantOnStrand, sequenceInterval);
            featureData.addFeature("canonical_donor", canonicalDonor);

            // `cryptic_donor` feature
            final double crypticDonor = crypticDonorScorer.score(donorBoundary.get(), variantOnStrand, sequenceInterval);
            featureData.addFeature("cryptic_donor", crypticDonor);
        } else {
            featureData.addFeature("canonical_donor", 0.);
            featureData.addFeature("cryptic_donor", 0.);
        }

        if (acceptorBoundary.isPresent()) {
            // `canonical_acceptor` feature
            final double canonicalAcceptor = canonicalAcceptorScorer.score(acceptorBoundary.get(), variantOnStrand, sequenceInterval);
            featureData.addFeature("canonical_acceptor", canonicalAcceptor);

            // `cryptic_acceptor` feature
            final double crypticAcceptor = crypticAcceptorScorer.score(acceptorBoundary.get(), variantOnStrand, sequenceInterval);
            featureData.addFeature("cryptic_acceptor", crypticAcceptor);
        } else {
            featureData.addFeature("canonical_acceptor", 0.);
            featureData.addFeature("cryptic_acceptor", 0.);
        }

        final double hexamerScore = hexamerFeatureCalculator.score(null, variant, sequenceInterval);
        featureData.addFeature("hexamer", hexamerScore);
        final double septamerScore = septamerFeatureCalculator.score(null, variant, sequenceInterval);
        featureData.addFeature("septamer", septamerScore);

        final double phylopScore = phyloPFeatureCalculator.score(null, variant, sequenceInterval);
        featureData.addFeature("phylop", phylopScore);

        final int donorOffset = getOffset(transcript.getExons().stream()
                        .map(e -> e.getInterval().getGenomeEndPos()),
                variantOnStrand.getGenomeInterval());
        featureData.addFeature("donor_offset", donorOffset);

        final int acceptorOffset = getOffset(transcript.getExons().stream()
                        .map(e -> e.getInterval().getGenomeBeginPos()),
                variantOnStrand.getGenomeInterval());
        featureData.addFeature("acceptor_offset", acceptorOffset);

        return SplicingAnnotationData.newBuilder()
                .featureData(featureData.build())
                .meanPhyloPScore(phylopScore)
                // this sucks a bit, but we'll live
                .putDonorCoordinates(locationData.getDonorRegion().orElse(null))
                .putAcceptorCoordinates(locationData.getAcceptorRegion().orElse(null))
                .build();
    }
}
