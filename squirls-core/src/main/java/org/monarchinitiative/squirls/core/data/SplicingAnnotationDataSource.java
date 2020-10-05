package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import java.util.Collection;
import java.util.Set;

public interface SplicingAnnotationDataSource {

    Collection<String> getTranscriptAccessionIds();

    /**
     * @return collection annotation with respect to all transcripts that overlap with the coordinates
     */
    <T extends SplicingPredictionData> Collection<T> getAnnotations(GenomeVariant variant);

    <T extends SplicingPredictionData> Collection<T> getAnnotations(GenomeVariant variant, Set<String> transcripts);

}
