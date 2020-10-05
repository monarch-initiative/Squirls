package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.Annotatable;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleAnnotatable implements Annotatable {

    private final GenomeVariant variant;
    private final SplicingTranscript transcript;
    private final Map<String, TrackRegion<?>> trackMap;
    private final Map<String, Object> features = new HashMap<>();
    private Metadata metadata;

    public SimpleAnnotatable(GenomeVariant variant,
                             SplicingTranscript transcript,
                             Map<String, TrackRegion<?>> tracks) {
        this.variant = variant;
        this.transcript = transcript;
        this.trackMap = Map.copyOf(tracks);
    }

    @Override
    public GenomeVariant getVariant() {
        return variant;
    }

    @Override
    public SplicingTranscript getTranscript() {
        return transcript;
    }

    @Override
    public Set<String> getTrackNames() {
        return Set.copyOf(trackMap.keySet());
    }

    @Override
    public <T extends TrackRegion<?>> T getTrack(String name, Class<T> clz) {
        return clz.cast(trackMap.get(name));
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Set<String> getFeatureNames() {
        return features.keySet();
    }

    @Override
    public <T> T getFeature(String featureName, Class<T> clz) {
        return clz.cast(features.get(featureName));
    }

    @Override
    public void putFeature(String name, Object value) {
        features.put(name, value);
    }
}
