package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * Splicing evaluator applies appropriate scoring functions to given variant (e.g. variant affecting canonical donor site
 * will not be evaluated with respect to canonical acceptor site).
 */
public interface SplicingEvaluator {

    /**
     * Evaluate {@code variant} with respect to given {@code transcript}.
     *
     * @param variant          variant to be evaluated
     * @param transcript       transcript with respect to which the variant is being evaluated
     * @param sequenceInterval reference sequence
     * @return splicing pathogenicity data
     */
    SplicingPathogenicityData evaluate(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval);
}
