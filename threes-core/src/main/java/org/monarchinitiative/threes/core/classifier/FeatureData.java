package org.monarchinitiative.threes.core.classifier;

import java.util.Optional;

public interface FeatureData {

    <T> Optional<T> getFeature(int featureIdx, Class<T> clz);

}
