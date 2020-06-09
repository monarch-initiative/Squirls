package org.monarchinitiative.threes.core;

import org.monarchinitiative.threes.core.classifier.Classifiable;

import java.util.HashMap;
import java.util.Map;
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
}

