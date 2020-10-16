package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.monarchinitiative.squirls.core.classifier.Prediction;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMutablePrediction that = (SimpleMutablePrediction) o;
        return Objects.equals(prediction, that.prediction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prediction);
    }

    @Override
    public String toString() {
        return "SimpleMutablePrediction{" +
                "prediction=" + prediction +
                '}';
    }
}
