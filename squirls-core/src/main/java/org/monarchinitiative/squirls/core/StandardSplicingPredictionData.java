package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;

import java.util.*;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardSplicingPredictionData that = (StandardSplicingPredictionData) o;
        return Objects.equals(variant, that.variant) &&
                Objects.equals(transcript, that.transcript) &&
                Objects.equals(featureMap, that.featureMap) &&
                Objects.equals(trackMap, that.trackMap) &&
                Objects.equals(prediction, that.prediction) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, transcript, featureMap, trackMap, prediction, metadata);
    }

    @Override
    public String toString() {
        return "StandardSplicingPredictionData{" +
                "variant=" + variant +
                ", transcript=" + transcript +
                ", featureMap=" + featureMap +
                ", trackMap=" + trackMap +
                ", prediction=" + prediction +
                ", metadata=" + metadata +
                '}';
    }
}
