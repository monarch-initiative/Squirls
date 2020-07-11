package org.monarchinitiative.squirls.core.classifier.transform.feature;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface MutableFeature {

    /**
     * @return set of all feature names available within this instance
     */
    Set<String> getFeatureNames();

    /**
     * Get feature value (check for presence before getting).
     *
     * @param featureName name of the feature
     * @param clz         cast the feature value to class {@link T}
     * @param <T>         class type
     * @return feature value
     * @throws NullPointerException if the <code>featureName</code> is not available.
     */
    <T> T getFeature(String featureName, Class<T> clz);

    /**
     * Store feature <code>value</code> with given <code>name</code>.
     *
     * @param name  feature name
     * @param value feature value
     */
    void putFeature(String name, Object value);

    /**
     * Get map with all available features.
     *
     * @return {@link Map} with all available features
     */
    default Map<String, Object> getFeatureMap() {
        return getFeatureNames().stream()
                .collect(Collectors.toMap(Function.identity(), name -> getFeature(name, Object.class)));
    }

}
