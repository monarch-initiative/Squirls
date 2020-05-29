package org.monarchinitiative.threes.core.classifier.transform.feature;

import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.PredictionException;

import java.util.Set;

/**
 * A class for performing some transformation of a single data point.
 *
 * @param <T>
 */
public interface FeatureTransformer<T extends FeatureData> {

    Set<String> usedFeatureNames();

    T transform(T instance) throws PredictionException;

}
