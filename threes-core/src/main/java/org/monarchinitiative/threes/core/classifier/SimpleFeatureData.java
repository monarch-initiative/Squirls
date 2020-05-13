package org.monarchinitiative.threes.core.classifier;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * POJO that contains all data required by {@link OverlordClassifier}.
 */
public class SimpleFeatureData implements FeatureData {

    private final Map<Integer, Object> features;

    private SimpleFeatureData(Map<Integer, Object> features) {
        this.features = features;
    }

    public static SimpleFeatureData of(Map<Integer, Object> features) {
        return new SimpleFeatureData(features);
    }

    @Override
    public <T> Optional<T> getFeature(int featureIdx, Class<T> clz) {
        return features.containsKey(featureIdx)
                ? Optional.of(clz.cast(features.get(featureIdx)))
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
