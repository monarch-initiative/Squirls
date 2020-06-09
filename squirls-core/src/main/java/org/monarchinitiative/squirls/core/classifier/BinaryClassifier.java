package org.monarchinitiative.squirls.core.classifier;

import java.util.Set;

/**
 * Class for labeling of an <code>instance</code> either as <code>positive</code> or <code>negative</code>.
 *
 * @param <T> <code>instance</code> type
 */
public interface BinaryClassifier<T extends Classifiable> {

    /**
     * @return set with expected feature names
     */
    Set<String> usedFeatureNames();

    /**
     * Predict class probabilities for given instance. The instance should contain all features that are required by
     * {@link #usedFeatureNames()}.
     *
     * @param instance instance used for prediction
     * @return probability that the <code>instance</code> belongs to the positive class
     * @throws PredictionException if a required feature is missing or if if there are any other problems in the
     *                             prediction process
     */
    double predictProba(T instance) throws PredictionException;
}
