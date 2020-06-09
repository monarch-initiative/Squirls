package org.monarchinitiative.squirls.core.classifier.io;

import java.util.List;
import java.util.Objects;

public class PipelineTransferModel {

    private List<String> featureNames;
    private List<Double> featureStatistics;
    private RandomForestTransferModel rf;

    public List<String> getFeatureNames() {
        return featureNames;
    }

    public void setFeatureNames(List<String> featureNames) {
        this.featureNames = featureNames;
    }

    public List<Double> getFeatureStatistics() {
        return featureStatistics;
    }

    public void setFeatureStatistics(List<Double> featureStatistics) {
        this.featureStatistics = featureStatistics;
    }

    public RandomForestTransferModel getRf() {
        return rf;
    }

    public void setRf(RandomForestTransferModel rf) {
        this.rf = rf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineTransferModel that = (PipelineTransferModel) o;
        return Objects.equals(featureNames, that.featureNames) &&
                Objects.equals(featureStatistics, that.featureStatistics) &&
                Objects.equals(rf, that.rf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureNames, featureStatistics, rf);
    }

    @Override
    public String toString() {
        return "PipelineTransferModel{" +
                "featureNames=" + featureNames +
                ", featureStatistics=" + featureStatistics +
                ", rf=" + rf +
                '}';
    }
}
