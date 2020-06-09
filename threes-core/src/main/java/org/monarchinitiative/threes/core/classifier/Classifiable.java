package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.classifier.transform.feature.MutableFeature;
import org.monarchinitiative.threes.core.classifier.transform.prediction.MutablePrediction;


/**
 * This interface describes objects that has all the information required to be used for prediction.
 */
public interface Classifiable extends MutableFeature, MutablePrediction {

}
