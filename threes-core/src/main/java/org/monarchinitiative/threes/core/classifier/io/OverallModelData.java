package org.monarchinitiative.threes.core.classifier.io;

import java.util.Objects;

public class OverallModelData {

    private double donorThreshold;
    private double acceptorThreshold;
    private RandomForestTransferModel donorClf;
    private RandomForestTransferModel acceptorClf;

    public RandomForestTransferModel getDonorClf() {
        return donorClf;
    }

    public void setDonorClf(RandomForestTransferModel donorClf) {
        this.donorClf = donorClf;
    }

    public RandomForestTransferModel getAcceptorClf() {
        return acceptorClf;
    }

    public void setAcceptorClf(RandomForestTransferModel acceptorClf) {
        this.acceptorClf = acceptorClf;
    }

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
