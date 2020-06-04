package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * Splicing evaluator applies scoring functions to given variant.
 */
public interface SplicingAnnotator {

    /**
     * Evaluate {@code variant} with respect to given {@code transcript}.
     *
     * @param variant          variant to be evaluated
     * @param transcript       transcript with respect to which the variant is being evaluated
     * @param sequenceInterval reference sequence
     * @return splicing pathogenicity data
     */
    SplicingAnnotationData evaluate(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequenceInterval);
}
