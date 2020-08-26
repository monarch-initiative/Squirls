package org.monarchinitiative.squirls.core.classifier.prediction_transform;

import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.MutablePrediction;

public class SimpleMutablePrediction implements MutablePrediction {

    private Prediction prediction;

    @Override
    public Prediction getPrediction() {
        return prediction;
    }

    @Override
    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }
}
