package org.monarchinitiative.threes.core.scoring;

import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;

/**
 * Splicing evaluator applies appropriate scoring functions to given variant (e.g. variant affecting canonical donor site
 * will not be evaluated with respect to canonical acceptor site).
 */
public interface SplicingEvaluator {

    SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval);

}
