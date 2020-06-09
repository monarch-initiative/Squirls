package org.monarchinitiative.squirls.core.scoring;

public interface SplicingAnnotator {

    /**
     * Evaluate the <code>data</code> and annotate with the available features.
     *
     * @return splicing pathogenicity data
     */
    <T extends Annotatable> T annotate(T data);

}
