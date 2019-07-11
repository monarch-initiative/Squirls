package org.monarchinitiative.threes.core.scoring;

import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The simplest evaluation strategy:<br>
 * <ul>
 * <li>find location of variant with respect to transcript</li>
 * <li>apply scorers for each location</li>
 * </ul>
 */
public class SimpleSplicingEvaluator implements SplicingEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSplicingEvaluator.class);

    private final ScorerFactory factory;

    private final SplicingTranscriptLocator transcriptLocator;

    private final GenomeCoordinatesFlipper flipper;

    public SimpleSplicingEvaluator(ScorerFactory factory, SplicingTranscriptLocator transcriptLocator, GenomeCoordinatesFlipper flipper) {
        this.transcriptLocator = transcriptLocator;
        this.factory = factory;
        this.flipper = flipper;
    }


    @Override
    public SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {

        try {
            final SplicingPathogenicityData.Builder resultBuilder = SplicingPathogenicityData.newBuilder();

            // Adjust sequence interval and variant to the transcript's strand
            if (variant.getCoordinates().getStrand() != transcript.getStrand()) {
                Optional<SplicingVariant> varOp = flipper.flip(variant);

                if (varOp.isPresent()) {
                    variant = varOp.get();
                } else {
                    // can't do anything more
                    return resultBuilder.build();
                }
            }
            if (sequence.getCoordinates().getStrand() != transcript.getStrand()) {
                Optional<SequenceInterval> seqOp = flipper.flip(sequence);
                if (seqOp.isPresent()) {
                    sequence = seqOp.get();
                } else {
                    // can't do anything more
                    return resultBuilder.build();
                }
            }


            // find where is the variant located with respect to given transcript
            final SplicingLocationData ld = transcriptLocator.locate(variant, transcript);


            // apply appropriate scorers

            SplicingLocationData.SplicingPosition position = ld.getPosition();
            final int featureIdx;
            if (position.equals(SplicingLocationData.SplicingPosition.DONOR)) {
                /*
                          EVALUATE VARIANT AFFECTING THE CANONICAL DONOR SITE
                 */
                // apply canonical donor & cryptic donor in canonical position
                featureIdx = ld.getIntronIdx(); // with respect to intron
                SplicingTernate donorTernate = SplicingTernate.of(variant, transcript.getIntrons().get(featureIdx), sequence);
                double donorScore = factory.scorerForStrategy(ScoringStrategy.CANONICAL_DONOR).apply(donorTernate);
                double cryptDonScore = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION).apply(donorTernate);
                resultBuilder.putScore(ScoringStrategy.CANONICAL_DONOR, donorScore)
                        .putScore(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, cryptDonScore);

            } else if (position.equals(SplicingLocationData.SplicingPosition.ACCEPTOR)) {
                /*
                          EVALUATE VARIANT AFFECTING THE CANONICAL ACCEPTOR SITE
                 */
                // apply canonical acceptor & cryptic acceptor in canonical position
                featureIdx = ld.getIntronIdx(); // with respect to intron
                SplicingTernate acceptorT = SplicingTernate.of(variant, transcript.getIntrons().get(featureIdx), sequence);
                double acceptorScore = factory.scorerForStrategy(ScoringStrategy.CANONICAL_ACCEPTOR).apply(acceptorT);
                double cryptAccScore = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION).apply(acceptorT);
                resultBuilder.putScore(ScoringStrategy.CANONICAL_ACCEPTOR, acceptorScore)
                        .putScore(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, cryptAccScore);

            } else if (position.equals(SplicingLocationData.SplicingPosition.EXON)) {
                /*
                          EVALUATE EXONIC VARIANT
                 */
                featureIdx = ld.getExonIdx();
                if (featureIdx != 0) {
                    // variant is not in the first exon, the exon has the acceptor site
                    // we're in the n-nth exon, so use the n-1-th intron to calculate the acceptor score
                    SplicingTernate crypticAccT = SplicingTernate.of(variant, transcript.getIntrons().get(featureIdx - 1), sequence);
                    double exonCryptAccSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR).apply(crypticAccT);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_ACCEPTOR, exonCryptAccSc);
                }
                if (featureIdx < (transcript.getExons().size() - 1)) {
                    // variant is not in the last exon, the exon has the donor site
                    SplicingTernate crypticDonT = SplicingTernate.of(variant, transcript.getIntrons().get(featureIdx), sequence);
                    double exonCryptDonSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR).apply(crypticDonT);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR, exonCryptDonSc);
                }
                // calculate septamer scores
                SplicingTernate exonTernate = SplicingTernate.of(variant, transcript.getExons().get(featureIdx), sequence);
                double smsScore = factory.scorerForStrategy(ScoringStrategy.SMS).apply(exonTernate);
                resultBuilder.putScore(ScoringStrategy.SMS, smsScore);
            } else if (position.equals(SplicingLocationData.SplicingPosition.INTRON)) {
                featureIdx = ld.getIntronIdx();
                SplicingTernate intronCryptT = SplicingTernate.of(variant, transcript.getIntrons().get(featureIdx), sequence);
                double intronCryptDonSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR).apply(intronCryptT);
                double intronCryptAccSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR).apply(intronCryptT);

                resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR, intronCryptDonSc)
                        .putScore(ScoringStrategy.CRYPTIC_ACCEPTOR, intronCryptAccSc);
            }


            return resultBuilder.build();
        } catch (Exception e) {
            // complain but don't crash the analysis
            LOGGER.warn("Error occurred when calculating splicing score for variant \n{}\n transcript \n{}", variant, transcript, e);
            return SplicingPathogenicityData.empty();
        }
    }
}
