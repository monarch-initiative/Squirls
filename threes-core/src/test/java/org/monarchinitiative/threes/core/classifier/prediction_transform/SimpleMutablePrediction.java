package org.monarchinitiative.threes.core.classifier.prediction_transform;

import org.monarchinitiative.threes.core.Prediction;
import org.monarchinitiative.threes.core.classifier.transform.prediction.MutablePrediction;

class SimpleMutablePrediction implements MutablePrediction {

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
