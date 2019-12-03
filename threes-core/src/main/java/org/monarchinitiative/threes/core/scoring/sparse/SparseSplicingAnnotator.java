package org.monarchinitiative.threes.core.scoring.sparse;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotator;
import org.monarchinitiative.threes.core.scoring.SplicingPathogenicityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Set;

/**
 * The simplest evaluation strategy:<br>
 * <ul>
 * <li>find location of variant with respect to transcript</li>
 * <li>apply scorers for each location</li>
 * </ul>
 */
public class SparseSplicingAnnotator implements SplicingAnnotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparseSplicingAnnotator.class);

    private final ScorerFactory factory;

    private final SplicingTranscriptLocator transcriptLocator;


    public SparseSplicingAnnotator(ScorerFactory factory, SplicingTranscriptLocator transcriptLocator) {
        this.transcriptLocator = transcriptLocator;
        this.factory = factory;
    }


    private SplicingPathogenicityData evaluate(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence, Set<ScoringStrategy> strategies) {
        // TODO - move this code into the calling method
        try {
            final SplicingPathogenicityData.Builder resultBuilder = SplicingPathogenicityData.builder();

            // Adjust sequence interval and variant to the transcript's strand
            GenomeVariant variantOnStrand = variant.withStrand(transcript.getStrand());

            // find where is the variant located with respect to given transcript
            final SplicingLocationData ld = transcriptLocator.locate(variantOnStrand, transcript);


            // apply appropriate scorers

            SplicingLocationData.SplicingPosition position = ld.getPosition();
            final int featureIdx;
            if (position.equals(SplicingLocationData.SplicingPosition.DONOR)) {
                /*
                          EVALUATE VARIANT AFFECTING THE CANONICAL DONOR SITE
                 */
                // apply canonical donor & cryptic donor in canonical position
                featureIdx = ld.getIntronIdx(); // with respect to intron
                SplicingTernate donorTernate = SplicingTernate.of(variantOnStrand, transcript.getIntrons().get(featureIdx), sequence);
                if (strategies.contains(ScoringStrategy.CANONICAL_DONOR)) {
                    double donorScore = factory.scorerForStrategy(ScoringStrategy.CANONICAL_DONOR).apply(donorTernate);
                    resultBuilder.putScore(ScoringStrategy.CANONICAL_DONOR.name(), donorScore);
                }
                if (strategies.contains(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION)) {
                    double cryptDonScore = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION).apply(donorTernate);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION.name(), cryptDonScore);
                }

            } else if (position.equals(SplicingLocationData.SplicingPosition.ACCEPTOR)) {
                /*
                          EVALUATE VARIANT AFFECTING THE CANONICAL ACCEPTOR SITE
                 */
                // apply canonical acceptor & cryptic acceptor in canonical position
                featureIdx = ld.getIntronIdx(); // with respect to intron
                SplicingTernate acceptorT = SplicingTernate.of(variantOnStrand, transcript.getIntrons().get(featureIdx), sequence);
                if (strategies.contains(ScoringStrategy.CANONICAL_ACCEPTOR)) {
                    double acceptorScore = factory.scorerForStrategy(ScoringStrategy.CANONICAL_ACCEPTOR).apply(acceptorT);
                    resultBuilder.putScore(ScoringStrategy.CANONICAL_ACCEPTOR.name(), acceptorScore);
                }
                if (strategies.contains(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION)) {
                    double cryptAccScore = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION).apply(acceptorT);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION.name(), cryptAccScore);
                }

            } else if (position.equals(SplicingLocationData.SplicingPosition.EXON)) {
                /*
                          EVALUATE EXONIC VARIANT
                 */
                featureIdx = ld.getExonIdx();
                if (featureIdx != 0 && strategies.contains(ScoringStrategy.CRYPTIC_ACCEPTOR)) {
                    // variant is not in the first exon, the exon has the acceptor site
                    // we're in the n-nth exon, so use the n-1-th intron to calculate the acceptor score
                    SplicingTernate crypticAccT = SplicingTernate.of(variantOnStrand, transcript.getIntrons().get(featureIdx - 1), sequence);
                    double exonCryptAccSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR).apply(crypticAccT);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_ACCEPTOR.name(), exonCryptAccSc);
                }
                if (featureIdx < (transcript.getExons().size() - 1) && strategies.contains(ScoringStrategy.CRYPTIC_DONOR)) {
                    // variant is not in the last exon, the exon has the donor site
                    SplicingTernate crypticDonT = SplicingTernate.of(variantOnStrand, transcript.getIntrons().get(featureIdx), sequence);
                    double exonCryptDonSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR).apply(crypticDonT);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR.name(), exonCryptDonSc);
                }

                if (strategies.contains(ScoringStrategy.SMS)) {
                    // calculate septamer scores
                    SplicingTernate exonTernate = SplicingTernate.of(variantOnStrand, transcript.getExons().get(featureIdx), sequence);
                    double smsScore = factory.scorerForStrategy(ScoringStrategy.SMS).apply(exonTernate);
                    resultBuilder.putScore(ScoringStrategy.SMS.name(), smsScore);
                }
            } else if (position.equals(SplicingLocationData.SplicingPosition.INTRON)) {
                /*
                          EVALUATE INTRONIC VARIANT
                 */
                featureIdx = ld.getIntronIdx();
                SplicingTernate intronCryptT = SplicingTernate.of(variantOnStrand, transcript.getIntrons().get(featureIdx), sequence);
                if (strategies.contains(ScoringStrategy.CRYPTIC_DONOR)) {
                    double intronCryptDonSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_DONOR).apply(intronCryptT);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR.name(), intronCryptDonSc);
                }
                if (strategies.contains(ScoringStrategy.CRYPTIC_ACCEPTOR)) {
                    double intronCryptAccSc = factory.scorerForStrategy(ScoringStrategy.CRYPTIC_ACCEPTOR).apply(intronCryptT);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_ACCEPTOR.name(), intronCryptAccSc);
                }
            }


            return resultBuilder.build();
        } catch (Exception e) {
            // complain but don't crash the analysis
            LOGGER.warn("Error occurred when calculating splicing score for variant \n{}\n transcript \n{}", variant, transcript, e);
            return SplicingPathogenicityData.empty();
        }
    }

    @Override
    public SplicingPathogenicityData evaluate(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {
        return this.evaluate(variant, transcript, sequenceInterval, ScoringStrategy.crypticAndCanonicalDonorAndAcceptor());
    }
}
