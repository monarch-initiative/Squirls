package org.monarchinitiative.sss.core.scoring;

import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.reference.SplicingLocationData;
import org.monarchinitiative.sss.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.sss.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.sss.core.scoring.scorers.ScorerFactory;

/**
 * Simplest evaluation strategy:<br>
 * <ul>
 * <li>find overlapping transcripts</li>
 * <li></li>
 * </ul>
 */
public class SimpleSplicingEvaluator implements SplicingEvaluator {

    private final GenomeSequenceAccessor sequenceAccessor;

    private final SplicingTranscriptLocator transcriptLocator;

    private final ScorerFactory factory;

    public SimpleSplicingEvaluator(GenomeSequenceAccessor sequenceAccessor, SplicingTranscriptLocator transcriptLocator, ScorerFactory factory) {
        this.sequenceAccessor = sequenceAccessor;
        this.transcriptLocator = transcriptLocator;
        this.factory = factory;
    }


    @Override
    public SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript) {

        final SequenceInterval si = sequenceAccessor.fetchSequence(transcript.getContig(), transcript.getTxBegin(), transcript.getTxEnd(), transcript.getStrand());
        final SplicingLocationData locationData = transcriptLocator.locate(variant, transcript);

        final SplicingPathogenicityData.Builder resultBuilder = SplicingPathogenicityData.newBuilder();

        switch (locationData.getPosition()) {
            case DONOR:
                final int d_i_i = locationData.getIntronIdx();
                double donorScore = factory.getCanonicalDonorScorer().score(variant, transcript.getIntrons().get(d_i_i), si);
                double cryptDonScore = factory.getCrypticDonorForVariantsInDonorSite().score(variant, transcript.getIntrons().get(d_i_i), si);
                resultBuilder.putScore(ScoringStrategy.CANONICAL_DONOR, donorScore)
                        .putScore(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, cryptDonScore);
                break;
            case ACCEPTOR:
                final int a_i_i = locationData.getIntronIdx();
                double acceptorScore = factory.getCanonicalAcceptorScorer().score(variant, transcript.getIntrons().get(a_i_i), si);
                double cryptAccScore = factory.getCrypticAcceptorForVariantsInAcceptorSite().score(variant, transcript.getIntrons().get(a_i_i), si);
                resultBuilder.putScore(ScoringStrategy.CANONICAL_ACCEPTOR, acceptorScore)
                        .putScore(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, cryptAccScore);
                break;
            case EXON:
                final int e_e_i = locationData.getExonIdx();
                if (e_e_i != 0) { // variant is not in the first exon
                    // use the previous intron to calculate acceptor score
                    final double exonCryptAccSc = factory.getCrypticAcceptorScorer().score(variant, transcript.getIntrons().get(e_e_i - 1), si);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_ACCEPTOR, exonCryptAccSc);
                } else if (transcript.getExons().size() - 1 != e_e_i) { // variant is not in the last exon
                    final double exonCryptDonSc = factory.getCrypticDonorScorer().score(variant, transcript.getIntrons().get(e_e_i), si);
                    resultBuilder.putScore(ScoringStrategy.CRYPTIC_DONOR, exonCryptDonSc);
                }
                break;
            case INTRON:
                final int i_i = locationData.getIntronIdx();
                final double intronCryptDonSc = factory.getCrypticDonorScorer().score(variant, transcript.getIntrons().get(i_i), si);
                final double intronCryptAccSc = factory.getCrypticAcceptorScorer().score(variant, transcript.getIntrons().get(i_i), si);

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
