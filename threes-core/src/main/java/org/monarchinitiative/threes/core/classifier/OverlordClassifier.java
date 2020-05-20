package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.Prediction;

import java.util.Set;

// TODO - rename once we have a name
public interface OverlordClassifier {

    /**
     * @return set with expected feature names
     */
    Set<String> usedFeatureNames();

    /**
     * Predict class label for given instance. The instance should contain all features that are required by
     * {@link #usedFeatureNames()}.
     *
     * @param instance instance used for prediction
     * @return class label
     * @throws PredictionException if a required feature is missing or if if there are any other problems in the
     *                             prediction process
     */
    Prediction predict(FeatureData instance) throws PredictionException;
}
