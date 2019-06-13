package org.monarchinitiative.threes.core.scoring;

import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.threes.core.scoring.scorers.ScorerFactory;

/**
 * Simplest evaluation strategy:<br>
 * <ul>
 * <li>find overlapping transcripts</li>
 * <li></li>
 * </ul>
 */
public class SimpleSplicingEvaluator implements SplicingEvaluator {

    private final SplicingTranscriptLocator transcriptLocator;

    private final ScorerFactory factory;

    public SimpleSplicingEvaluator(SplicingTranscriptLocator transcriptLocator, ScorerFactory factory) {
        this.transcriptLocator = transcriptLocator;
        this.factory = factory;
    }


    @Override
    public SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {

        // find where is the variant located with respect to given transcript
        final SplicingLocationData locationData = transcriptLocator.locate(variant, transcript);
        final SplicingPathogenicityData.Builder resultBuilder = SplicingPathogenicityData.newBuilder();

        // apply appropriate scorers
        switch (locationData.getPosition()) {
            case DONOR:
                final int d_i_i = locationData.getIntronIdx();

                double donorScore = factory.scorerForStrategy(ScoringStrategy.CANONICAL_DONOR)
                        .score(variant, transcript.getIntrons().get(d_i_i), sequenceInterval);
                double cryptDonScore = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION)
                        .score(variant, transcript.getIntrons().get(d_i_i), sequenceInterval);
                resultBuilder.putScore(ScoringStrategy.CANONICAL_DONOR, donorScore)
                        .putScore(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, cryptDonScore);
                break;
            case ACCEPTOR:
                final int a_i_i = locationData.getIntronIdx();
                double acceptorScore = factory.scorerForStrategy(ScoringStrategy.CANONICAL_ACCEPTOR).score(variant, transcript.getIntrons().get(a_i_i), sequenceInterval);
                double cryptAccScore = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION).score(variant, transcript.getIntrons().get(a_i_i), sequenceInterval);
                resultBuilder.putScore(ScoringStrategy.CANONICAL_ACCEPTOR, acceptorScore)
                        .putScore(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, cryptAccScore);
                break;
            case EXON:
                final int e_e_i = locationData.getExonIdx();
                if (e_e_i != 0) { // variant is not in the first exon
                    // use the previous intron to calculate acceptor score
                    final double exonCryptAccSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR).score(variant, transcript.getIntrons().get(e_e_i - 1), sequenceInterval);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_ACCEPTOR, exonCryptAccSc);
                } else if (transcript.getExons().size() - 1 != e_e_i) { // variant is not in the last exon
                    final double exonCryptDonSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR).score(variant, transcript.getIntrons().get(e_e_i), sequenceInterval);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR, exonCryptDonSc);
                }
                break;
            case INTRON:
                final int i_i = locationData.getIntronIdx();
                final double intronCryptDonSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR).score(variant, transcript.getIntrons().get(i_i), sequenceInterval);
                final double intronCryptAccSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR).score(variant, transcript.getIntrons().get(i_i), sequenceInterval);

                resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR, intronCryptDonSc)
                        .putScore(ScoringStrategy.CRYPTIC_ACCEPTOR, intronCryptAccSc);

                break;
            default:
                // no-op
                break;

        }
        return resultBuilder.build();
    }
}
