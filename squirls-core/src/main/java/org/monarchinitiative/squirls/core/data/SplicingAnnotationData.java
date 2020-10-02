package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;

import java.util.List;
import java.util.Set;

public interface SplicingAnnotationData {

    SplicingTranscript getTranscript();

    Set<String> getTrackNames();

    <T> TrackRegion<T> getTrack(String trackName);

    default <T> List<T> getTrackForInterval(String trackName, GenomeInterval interval) {
        final TrackRegion<T> track = getTrack(trackName);
        return track.getValues(interval);
    }

}
