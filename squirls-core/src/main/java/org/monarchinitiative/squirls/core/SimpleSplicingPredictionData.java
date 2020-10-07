package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;

import java.util.*;

public class SimpleSplicingPredictionData implements SplicingPredictionData {

    private final GenomeVariant variant;

    private final SplicingTranscript transcript;
    private final Map<String, Object> featureMap;
    private Prediction prediction;
    private Metadata metadata;

    private SimpleSplicingPredictionData(GenomeVariant variant, SplicingTranscript transcript) {
        this.variant = variant;
        this.transcript = transcript;
        this.featureMap = Collections.synchronizedMap(new HashMap<>());
    }

    public static SimpleSplicingPredictionData copyOf(SplicingPredictionData other) {
        final SimpleSplicingPredictionData data = new SimpleSplicingPredictionData(other.getVariant(), other.getTranscript());
        data.setMetadata(other.getMetadata());
        data.setPrediction(other.getPrediction());
        data.featureMap.putAll(other.getFeatureMap());

        return data;
    }

    public static SimpleSplicingPredictionData of(GenomeVariant variant, SplicingTranscript transcript) {
        return new SimpleSplicingPredictionData(variant, transcript);
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
        return Set.of();
    }

    @Override
    public <T extends TrackRegion<?>> T getTrack(String name, Class<T> clz) {
        return null;
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
        return Set.copyOf(featureMap.keySet());
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
        SimpleSplicingPredictionData that = (SimpleSplicingPredictionData) o;
        return Objects.equals(variant, that.variant) &&
                Objects.equals(transcript, that.transcript) &&
                Objects.equals(prediction, that.prediction) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(featureMap, that.featureMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, transcript, prediction, metadata, featureMap);
    }

    @Override
    public String toString() {
        return "SimpleSplicingPredictionData{" +
                "variant=" + variant +
                ", transcript=" + transcript +
                ", prediction=" + prediction +
                ", metadata=" + metadata +
                ", featureMap=" + featureMap +
                '}';
    }
}
