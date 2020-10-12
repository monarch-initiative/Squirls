package org.monarchinitiative.squirls.core.scoring;

/**
 * Implementors calculate a set of features for each {@link Annotatable}.
 */
public interface SplicingAnnotator {

    /**
     * Evaluate the <code>data</code> and annotate with the available features.
     *
     * @return splicing pathogenicity data
     */
    <T extends Annotatable> T annotate(T data);

}
