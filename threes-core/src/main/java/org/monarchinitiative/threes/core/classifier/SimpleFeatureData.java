package org.monarchinitiative.threes.core.classifier;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * POJO that contains all data required by {@link OverlordClassifier}.
 */
public class SimpleFeatureData implements FeatureData {

    private final Map<String, Object> features;

    private SimpleFeatureData(Map<String, Object> features) {
        this.features = features;
    }

    public static SimpleFeatureData of(Map<String, Object> features) {
        return new SimpleFeatureData(features);
    }


    @Override
    public Set<String> getFeatureNames() {
        return features.keySet();
    }

    @Override
    public <T> Optional<T> getFeature(String featureName, Class<T> clz) {
        return features.containsKey(featureName)
                ? Optional.of(clz.cast(features.get(featureName)))
                : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleFeatureData that = (SimpleFeatureData) o;
        return Objects.equals(features, that.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(features);
    }

    @Override
    public String toString() {
        return "SimpleFeatureData{" +
                "features=" + features +
                '}';
    }
}
