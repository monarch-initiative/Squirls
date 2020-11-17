package org.monarchinitiative.squirls.core.classifier.transform.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    default void putAllFeatures(Map<String, ?> featureMap) {
        featureMap.forEach(this::putFeature);
    }

    /**
     * Get map with all available features.
     *
     * @return {@link Map} with all available features
     */
    default Map<String, Double> getFeatureMap() {
        Set<String> featureNames = getFeatureNames();
        HashMap<String, Double> featureMap = new HashMap<>(featureNames.size());

        for (String featureName : featureNames) {
            Object rawFeature = getFeature(featureName, Object.class);
            if (rawFeature instanceof Double) {
                featureMap.put(featureName, (Double) rawFeature);
            } else if (rawFeature instanceof Integer) {
                double doubleValue = ((Integer) rawFeature).doubleValue();
                featureMap.put(featureName, doubleValue);
            } else {
                throw new RuntimeException("Unexpected type " + rawFeature.getClass() + " for feature `" + featureName + '`');
            }
        }
        return featureMap;
    }

}
