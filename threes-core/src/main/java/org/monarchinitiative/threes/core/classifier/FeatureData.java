package org.monarchinitiative.threes.core.classifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents a single instance of training data.
 */
public class FeatureData {

    private final Map<String, Double> featureMap;

    private FeatureData(Builder<?> builder) {
        featureMap = builder.featureMap;
    }

    public static <T extends FeatureData> Builder<T> builder() {
        return new Builder<>(Map.of());
    }

    public Set<String> getFeatureNames() {
        return featureMap.keySet();
    }

    public Builder<FeatureData> toBuilder() {
        return new Builder<>(featureMap);
    }

    public <T> T getFeature(String featureName, Class<T> clz) {
        return clz.cast(featureMap.get(featureName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureData that = (FeatureData) o;
        return Objects.equals(featureMap, that.featureMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureMap);
    }

    @Override
    public String toString() {
        return "SplicingFeatureData{" +
                "featureMap=" + featureMap +
                '}';
    }

    public static final class Builder<T extends FeatureData> {
        private final Map<String, Double> featureMap = new HashMap<>();

        private Builder(Map<String, Double> featureMap) {
            this.featureMap.putAll(featureMap);
        }

        public Builder<T> addFeature(String featureName, double feature) {
            this.featureMap.put(featureName, feature);
            return this;
        }

        public Builder<T> addFeatures(Map<String, Double> features) {
            this.featureMap.putAll(features);
            return this;
        }

        public FeatureData build() {
            return new FeatureData(this);
        }
    }

}
