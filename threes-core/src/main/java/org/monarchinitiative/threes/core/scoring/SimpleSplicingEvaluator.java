package org.monarchinitiative.threes.core.scoring;

import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.threes.core.scoring.scorers.ScorerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The simplest evaluation strategy:<br>
 * <ul>
 * <li>find location of variant with respect to transcript</li>
 * <li>apply scorers for each location</li>
 * </ul>
 */
public class SimpleSplicingEvaluator implements SplicingEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSplicingEvaluator.class);

    private final SplicingTranscriptLocator transcriptLocator;

    private final ScorerFactory factory;

    public SimpleSplicingEvaluator(SplicingTranscriptLocator transcriptLocator, ScorerFactory factory) {
        this.transcriptLocator = transcriptLocator;
        this.factory = factory;
    }


    @Override
    public SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {
        try {
            // find where is the variant located with respect to given transcript
            final SplicingLocationData locationData = transcriptLocator.locate(variant, transcript);
            final SplicingPathogenicityData.Builder resultBuilder = SplicingPathogenicityData.newBuilder();

            // apply appropriate scorers
            switch (locationData.getPosition()) {
                case DONOR:
                    final int d_i_i = locationData.getIntronIdx();
                    final SplicingTernate donorT = SplicingTernate.of(variant, transcript.getIntrons().get(d_i_i), sequenceInterval);
                    double donorScore = factory.scorerForStrategy(ScoringStrategy.CANONICAL_DONOR).apply(donorT);
                    double cryptDonScore = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION).apply(donorT);
                    resultBuilder.putScore(ScoringStrategy.CANONICAL_DONOR, donorScore)
                            .putScore(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, cryptDonScore);
                    break;
                case ACCEPTOR:
                    final int a_i_i = locationData.getIntronIdx();
                    final SplicingTernate acceptorT = SplicingTernate.of(variant, transcript.getIntrons().get(a_i_i), sequenceInterval);
                    double acceptorScore = factory.scorerForStrategy(ScoringStrategy.CANONICAL_ACCEPTOR).apply(acceptorT);
                    double cryptAccScore = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION).apply(acceptorT);
                    resultBuilder.putScore(ScoringStrategy.CANONICAL_ACCEPTOR, acceptorScore)
                            .putScore(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, cryptAccScore);
                    break;
                case EXON:
                    final int e_e_i = locationData.getExonIdx();
                    if (e_e_i != 0) { // variant is not in the first exon
                        // use the previous intron to calculate acceptor score
                        final SplicingTernate crypticAccT = SplicingTernate.of(variant, transcript.getIntrons().get(e_e_i - 1), sequenceInterval);
                        final double exonCryptAccSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR).apply(crypticAccT);
                        resultBuilder.putScore(ScoringStrategy.CRYPTIC_ACCEPTOR, exonCryptAccSc);
                    } else if (transcript.getExons().size() - 1 != e_e_i) { // variant is not in the last exon
                        final SplicingTernate crypticDonT = SplicingTernate.of(variant, transcript.getIntrons().get(e_e_i), sequenceInterval);
                        final double exonCryptDonSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR).apply(crypticDonT);
                        resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR, exonCryptDonSc);
                    }
                    break;
                case INTRON:
                    final int i_i = locationData.getIntronIdx();
                    final SplicingTernate intronCryptT = SplicingTernate.of(variant, transcript.getIntrons().get(i_i), sequenceInterval);
                    final double intronCryptDonSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR).apply(intronCryptT);
                    final double intronCryptAccSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR).apply(intronCryptT);

                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR, intronCryptDonSc)
                            .putScore(ScoringStrategy.CRYPTIC_ACCEPTOR, intronCryptAccSc);

                    break;
                default:
                    // no-op
                    break;

            }
            return resultBuilder.build();
        } catch (Exception e) {
            // complain but don't crash the analysis
            LOGGER.warn("Error occurred when calculating splicing score for variant \n{}\n transcript \n{}", variant, transcript, e);
            return SplicingPathogenicityData.empty();
        }
    }
}
