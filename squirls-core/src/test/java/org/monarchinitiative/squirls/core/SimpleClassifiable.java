package org.monarchinitiative.squirls.core;

import org.monarchinitiative.squirls.core.classifier.Classifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SimpleClassifiable implements Classifiable {

    private final Map<String, Object> featureMap;
    private Prediction prediction;

    public SimpleClassifiable(Map<String, Object> featureMap) {
        this.featureMap = new HashMap<>(featureMap);
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
    public Prediction getPrediction() {
        return prediction;
    }

    @Override
    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleClassifiable that = (SimpleClassifiable) o;
        return Objects.equals(featureMap, that.featureMap) &&
                Objects.equals(prediction, that.prediction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureMap, prediction);
    }

    @Override
    public String toString() {
        return "SimpleClassifiable{" +
                "featureMap=" + featureMap +
                ", prediction=" + prediction +
                '}';
    }
}

