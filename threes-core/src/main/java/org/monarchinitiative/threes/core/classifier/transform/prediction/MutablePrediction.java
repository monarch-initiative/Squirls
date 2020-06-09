package org.monarchinitiative.threes.core.classifier.transform.prediction;

import org.monarchinitiative.threes.core.Prediction;

/**
 * Implementors represent data points that can be used by {@link PredictionTransformer}, in order to transform the
 * {@link Prediction}.
 */
public interface MutablePrediction {

    Prediction getPrediction();

    void setPrediction(Prediction prediction);
}
