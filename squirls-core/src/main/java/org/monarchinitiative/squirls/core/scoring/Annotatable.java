package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Metadata;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.transform.feature.MutableFeature;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;

import java.util.Set;

/**
 * This interface describes objects that can be annotated with features by {@link SplicingAnnotator} before being
 * subjected to prediction by {@link SquirlsClassifier}.
 */
public interface Annotatable extends MutableFeature {

    GenomeVariant getVariant();

    SplicingTranscript getTranscript();

    Set<String> getTrackNames();

    <T extends TrackRegion<?>> T getTrack(String name, Class<T> clz);

    Metadata getMetadata();

    void setMetadata(Metadata metadata);


}
