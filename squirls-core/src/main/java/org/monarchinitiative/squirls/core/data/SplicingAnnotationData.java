package org.monarchinitiative.squirls.core.data;

import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;

import java.util.Collection;
import java.util.Map;

/**
 * Classes that implement this interface provide transcripts and tracks available for a single gene.
 */
public interface SplicingAnnotationData {

    /**
     * @return collection of transcripts that belong to a gene.
     */
    Collection<SplicingTranscript> getTranscripts();

    /**
     * @return map of tracks that belong to a gene. The tracks are grouped by the name
     */
    Map<String, ? extends TrackRegion<?>> getTracks();

    /**
     * Get track with given {@code trackName}
     *
     * @param trackName name of the track
     * @param clz       type of the track
     * @return the track
     */
    default <T extends TrackRegion<?>> T getTrack(String trackName, Class<T> clz) {
        return clz.cast(getTracks().get(trackName));
    }

}
