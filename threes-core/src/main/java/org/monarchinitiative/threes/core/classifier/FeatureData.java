package org.monarchinitiative.threes.core.classifier;

import java.util.Optional;
import java.util.Set;

public interface FeatureData {

    Set<String> getFeatureNames();

    <T> Optional<T> getFeature(String featureName, Class<T> clz);

}
