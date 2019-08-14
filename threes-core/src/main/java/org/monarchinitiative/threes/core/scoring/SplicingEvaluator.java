package org.monarchinitiative.threes.core.scoring;

import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;

import java.util.Set;

/**
 * Splicing evaluator applies appropriate scoring functions to given variant (e.g. variant affecting canonical donor site
 * will not be evaluated with respect to canonical acceptor site).
 */
public interface SplicingEvaluator {

    /**
     * Evaluate {@code variant} with respect to given {@code transcript} using following scoring strategies:
     * <ul>
     * <li>{@link ScoringStrategy#CANONICAL_DONOR}</li>
     * <li>{@link ScoringStrategy#CRYPTIC_DONOR}</li>
     * <li>{@link ScoringStrategy#CRYPTIC_DONOR_IN_CANONICAL_POSITION}</li>
     * <li>{@link ScoringStrategy#CANONICAL_ACCEPTOR}</li>
     * <li>{@link ScoringStrategy#CRYPTIC_ACCEPTOR}</li>
     * <li>{@link ScoringStrategy#CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION}</li>
     * </ul>
     *
     * @param variant          variant adjusted to {@code transcript}'s trand
     * @param transcript       with respect to which the variant is evaluated
     * @param sequenceInterval reference sequence
     * @return splicing pathogenicity data
     */
    default SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval) {
        return evaluate(variant, transcript, sequenceInterval, ScoringStrategy.crypticAndCanonicalDonorAndAcceptor());
    }

    /**
     * Evaluate {@code variant} with respect to given {@code transcript}.
     *
     * @param variant          variant adjusted to {@code transcript}'s trand
     * @param transcript       with respect to which the variant is evaluated
     * @param sequenceInterval reference sequence
     * @param strategies       {@link ScoringStrategy}(ies) to apply
     * @return splicing pathogenicity data
     */
    SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval, Set<ScoringStrategy> strategies);
}
