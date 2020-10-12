package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.monarchinitiative.squirls.core.classifier.Named;

/**
 * The implementors perform some transformation (e.g. rescaling probabilities using logistic regression) with the
 * {@link MutablePrediction}.
 */
public interface PredictionTransformer extends Named {

    <T extends MutablePrediction> T transform(T data);

}
