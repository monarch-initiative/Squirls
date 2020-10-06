package org.monarchinitiative.squirls.core.data;

import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The simplest implementation of {@link SplicingAnnotationData} specification.
 */
class SplicingAnnotationDataImpl implements SplicingAnnotationData {

    private final Set<SplicingTranscript> transcripts;

    private final Map<String, ? extends TrackRegion<?>> trackMap;

    private SplicingAnnotationDataImpl(Set<SplicingTranscript> transcripts,
                                       Map<String, ? extends TrackRegion<?>> trackMap) {
        this.transcripts = transcripts;
        this.trackMap = trackMap;
    }

    public static SplicingAnnotationDataImpl of(Set<SplicingTranscript> transcripts,
                                                Map<String, ? extends TrackRegion<?>> trackMap) {
        return new SplicingAnnotationDataImpl(transcripts, trackMap);
    }

    @Override
    public Collection<SplicingTranscript> getTranscripts() {
        return transcripts;
    }

    @Override
    public Map<String, ? extends TrackRegion<?>> getTracks() {
        return trackMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplicingAnnotationDataImpl that = (SplicingAnnotationDataImpl) o;
        return Objects.equals(transcripts, that.transcripts) &&
                Objects.equals(trackMap, that.trackMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transcripts, trackMap);
    }

    @Override
    public String toString() {
        return "SplicingAnnotationDataImpl{" +
                "transcripts=" + transcripts +
                ", trackMap=" + trackMap +
                '}';
    }
}
