package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Metadata;
import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class StandardSplicingPredictionData implements SplicingPredictionData {

    private final GenomeVariant variant;

    private final SplicingTranscript transcript;

    private final Map<String, Object> featureMap;

    private final Map<String, ? extends TrackRegion<?>> trackMap;

    private Prediction prediction;

    private Metadata metadata;

    private StandardSplicingPredictionData(GenomeVariant variant,
                                           SplicingTranscript transcript,
                                           Map<String, ? extends TrackRegion<?>> trackMap) {
        this.variant = variant;
        this.transcript = transcript;
        this.featureMap = Collections.synchronizedMap(new HashMap<>());
        this.trackMap = Map.copyOf(trackMap);
    }

    public static StandardSplicingPredictionData of(GenomeVariant variant,
                                                    SplicingTranscript transcript,
                                                    Map<String, ? extends TrackRegion<?>> trackMap) {
        return new StandardSplicingPredictionData(variant, transcript, trackMap);
    }

    @Override
    public Prediction getPrediction() {
        return prediction;
    }

    @Override
    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
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
        return featureMap.keySet();
    }

    @Override
    public <T> T getFeature(String featureName, Class<T> clz) {
        return clz.cast(featureMap.get(featureName));
    }

    @Override
    public void putFeature(String name, Object value) {
        featureMap.put(name, value);
    }

}
