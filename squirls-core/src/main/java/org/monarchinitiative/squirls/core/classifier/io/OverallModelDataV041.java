package org.monarchinitiative.squirls.core.classifier.io;

import java.util.List;
import java.util.Objects;

public class OverallModelDataV041 implements OverallModelData {

    private double donorThreshold;
    private double acceptorThreshold;
    private PipelineTransferModel donorClf;
    private PipelineTransferModel acceptorClf;
    private List<List<Double>> slope;
    private List<Double> intercept;

    @Override
    public double getDonorThreshold() {
        return donorThreshold;
    }

    public void setDonorThreshold(double donorThreshold) {
        this.donorThreshold = donorThreshold;
    }

    @Override
    public double getAcceptorThreshold() {
        return acceptorThreshold;
    }

    public void setAcceptorThreshold(double acceptorThreshold) {
        this.acceptorThreshold = acceptorThreshold;
    }

    @Override
    public PipelineTransferModel getDonorClf() {
        return donorClf;
    }

    public void setDonorClf(PipelineTransferModel donorClf) {
        this.donorClf = donorClf;
    }

    @Override
    public PipelineTransferModel getAcceptorClf() {
        return acceptorClf;
    }

    public void setAcceptorClf(PipelineTransferModel acceptorClf) {
        this.acceptorClf = acceptorClf;
    }

    @Override
    public List<List<Double>> getSlope() {
        return slope;
    }

    public void setSlope(List<List<Double>> slope) {
        this.slope = slope;
    }

    @Override
    public List<Double> getIntercept() {
        return intercept;
    }

    public void setIntercept(List<Double> intercept) {
        this.intercept = intercept;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverallModelDataV041 that = (OverallModelDataV041) o;
        return Double.compare(that.donorThreshold, donorThreshold) == 0 &&
                Double.compare(that.acceptorThreshold, acceptorThreshold) == 0 &&
                Objects.equals(that.intercept, intercept) &&
                Objects.equals(donorClf, that.donorClf) &&
                Objects.equals(acceptorClf, that.acceptorClf) &&
                Objects.equals(slope, that.slope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donorThreshold, acceptorThreshold, donorClf, acceptorClf, slope, intercept);
    }

    @Override
    public String toString() {
        return "OverallModelDataV041{" +
                "donorThreshold=" + donorThreshold +
                ", acceptorThreshold=" + acceptorThreshold +
                ", donorClf=" + donorClf +
                ", acceptorClf=" + acceptorClf +
                ", slope=" + slope +
                ", intercept=" + intercept +
                '}';
    }
}

