package org.monarchinitiative.threes.core.classifier.transform.prediction;

import org.monarchinitiative.threes.core.classifier.Prediction;

public interface PredictionTransformer {

    Prediction transform(Prediction prediction);
}
