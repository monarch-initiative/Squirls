package org.monarchinitiative.squirls.core.classifier.transform.feature;

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

}
