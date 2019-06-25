package org.monarchinitiative.threes.core.scoring;

import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;

/**
 * Splicing evaluator applies appropriate scoring functions to given variant (e.g. variant affecting canonical donor site
 * will not be evaluated with respect to canonical acceptor site).
 */
public interface SplicingEvaluator {

    /**
     * Evaluate {@code variant} with respect to given {@code transcript}.
     *
     * @param variant          variant adjusted to {@code transcript}'s trand
     * @param transcript       with respect to which the variant is evaluated
     * @param sequenceInterval reference sequence
     * @return splicing pathogenicity data
     */
    SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval);

}
