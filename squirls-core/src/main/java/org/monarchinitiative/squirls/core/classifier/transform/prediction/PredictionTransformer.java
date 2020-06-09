package org.monarchinitiative.squirls.core.classifier.transform.prediction;

/**
 * The implementors perform some transformation (e.g. rescaling probabilities using logistic regression) with the
 * {@link MutablePrediction}.
 */
public interface PredictionTransformer {

    <T extends MutablePrediction> T transform(T data);

}
