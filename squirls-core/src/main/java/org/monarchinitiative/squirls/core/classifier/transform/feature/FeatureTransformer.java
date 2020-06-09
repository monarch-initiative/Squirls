package org.monarchinitiative.squirls.core.classifier.transform.feature;

import org.monarchinitiative.squirls.core.classifier.PredictionException;

import java.util.Set;

/**
 * A class for performing some transformation of a single data point.
 *
 * @param <T>
 */
public interface FeatureTransformer<T extends MutableFeature> {

    Set<String> usedFeatureNames();

    T transform(T instance) throws PredictionException;

}
