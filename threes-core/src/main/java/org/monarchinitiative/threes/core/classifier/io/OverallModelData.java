package org.monarchinitiative.threes.core.classifier.io;

import java.util.Objects;

public class OverallModelData {

    private double donorThreshold;
    private double acceptorThreshold;
    private PipelineTransferModel donorClf;
    private PipelineTransferModel acceptorClf;

    public double getDonorThreshold() {
        return donorThreshold;
    }

    public void setDonorThreshold(double donorThreshold) {
        this.donorThreshold = donorThreshold;
    }

    public double getAcceptorThreshold() {
        return acceptorThreshold;
    }

    public void setAcceptorThreshold(double acceptorThreshold) {
        this.acceptorThreshold = acceptorThreshold;
    }

    public PipelineTransferModel getDonorClf() {
        return donorClf;
    }

    public void setDonorClf(PipelineTransferModel donorClf) {
        this.donorClf = donorClf;
    }

    public PipelineTransferModel getAcceptorClf() {
        return acceptorClf;
    }

    public void setAcceptorClf(PipelineTransferModel acceptorClf) {
        this.acceptorClf = acceptorClf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverallModelData that = (OverallModelData) o;
        return Double.compare(that.donorThreshold, donorThreshold) == 0 &&
                Double.compare(that.acceptorThreshold, acceptorThreshold) == 0 &&
                Objects.equals(donorClf, that.donorClf) &&
                Objects.equals(acceptorClf, that.acceptorClf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donorThreshold, acceptorThreshold, donorClf, acceptorClf);
    }

    @Override
    public String toString() {
        return "OverallModelData{" +
                "donorThreshold=" + donorThreshold +
                ", acceptorThreshold=" + acceptorThreshold +
                ", donorClf=" + donorClf +
                ", acceptorClf=" + acceptorClf +
                '}';
    }
}
