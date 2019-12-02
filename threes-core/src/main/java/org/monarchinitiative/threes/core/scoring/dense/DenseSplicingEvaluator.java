package org.monarchinitiative.threes.core.scoring.dense;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.threes.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.threes.core.scoring.SplicingEvaluator;
import org.monarchinitiative.threes.core.scoring.SplicingPathogenicityData;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Optional;


public class DenseSplicingEvaluator implements SplicingEvaluator {

    private static final int MAX_BP_IN_INTRON = 50;

    private final SplicingTranscriptLocator transcriptLocator;

    private final CanonicalDonorScorer canonicalDonorScorer;

    private final CanonicalAcceptorScorer canonicalAcceptorScorer;

    private final CrypticDonorScorer crypticDonorScorer;

    private final CrypticAcceptorScorer crypticAcceptorScorer;

    public DenseSplicingEvaluator(SplicingPwmData splicingPwmData) {
        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        AlleleGenerator generator = new AlleleGenerator(calculator.getSplicingParameters());

        transcriptLocator = new NaiveSplicingTranscriptLocator(calculator.getSplicingParameters());

        canonicalDonorScorer = new CanonicalDonorScorer(calculator, generator);
        canonicalAcceptorScorer = new CanonicalAcceptorScorer(calculator, generator);
        crypticDonorScorer = new CrypticDonorScorer(calculator, generator);
        crypticAcceptorScorer = new CrypticAcceptorScorer(calculator, generator);
    }

    @Override
    public SplicingPathogenicityData evaluate(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {

        final GenomeVariant variantOnStrand = variant.withStrand(transcript.getStrand());
        final SplicingLocationData locationData = transcriptLocator.locate(variant, transcript);

        Optional<GenomePosition> donorBoundary = locationData.getDonorBoundary();
        Optional<GenomePosition> acceptorBoundary = locationData.getAcceptorBoundary();


        final SplicingPathogenicityData.Builder builder = SplicingPathogenicityData.builder();

        donorBoundary.ifPresent(donorAnchor -> builder.putScore(canonicalDonorScorer.getName(), canonicalDonorScorer.score(donorAnchor, variantOnStrand, sequenceInterval))
                .putScore(crypticDonorScorer.getName(), crypticDonorScorer.score(donorAnchor, variantOnStrand, sequenceInterval)));

        acceptorBoundary.ifPresent(acceptorAnchor -> builder.putScore(canonicalAcceptorScorer.getName(), canonicalAcceptorScorer.score(acceptorAnchor, variantOnStrand, sequenceInterval))
                .putScore(crypticAcceptorScorer.getName(), crypticAcceptorScorer.score(acceptorAnchor, variantOnStrand, sequenceInterval)));

        return builder.build();

    }
}
