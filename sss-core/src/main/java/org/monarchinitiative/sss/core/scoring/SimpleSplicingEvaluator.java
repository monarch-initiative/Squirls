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
                double donorScore = factory.getCanonicalDonorScorer().score(variant, transcript.getIntrons().get(locationData.getFeatureIndex()), si);
                final double cryptDonorScore = factory.getCrypticDonorScorer().score(variant, transcript.getIntrons().get(locationData.getFeatureIndex()), si);
                resultBuilder.putScore(ScoringStrategy.CANONICAL_DONOR, donorScore)
                        .putScore(ScoringStrategy.CRYPTIC_DONOR, cryptDonorScore);
                break;
            case ACCEPTOR:
                double acceptorScore = factory.getCanonicalAcceptorScorer().score(variant, transcript.getIntrons().get(locationData.getFeatureIndex()), si);
                resultBuilder.putScore(ScoringStrategy.CANONICAL_ACCEPTOR, acceptorScore);
                // TODO - add cryptic acceptor scorer
                break;
            case EXON:
                // TODO - add cryptic donor & acceptor scorer
                break;
            case INTRON:
                // TODO - contemplate
                break;
            default:
                // no-op
                break;

        }
        return resultBuilder.build();
    }
}
