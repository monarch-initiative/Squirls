package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.Prediction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PredictionImpl implements Prediction {

    private final Map<Culprit, Fragment> data;

    private PredictionImpl(Builder builder) {
        data = Map.copyOf(builder.data);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean isPathogenic() {
        return data.values().stream().anyMatch(Fragment::isPathogenic);
    }

    @Override
    public double getPathoProba() {
        return data.values().stream()
                .filter(Fragment::isPathogenic)
                .mapToDouble(Fragment::getPathoProba)
                .max()
                .orElse(0.);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredictionImpl that = (PredictionImpl) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }


    @Override
    public String toString() {
        return "PredictionImpl{" +
                "data=" + data +
                '}';
    }

    private enum Culprit {
        DONOR,
        ACCEPTOR
    }


    private static class Fragment {
        private final double pathoProba;

        private final double threshold;

        public Fragment(double pathoProba, double threshold) {
            this.pathoProba = pathoProba;
            this.threshold = threshold;
        }

        public double getThreshold() {
            return threshold;
        }

        public double getPathoProba() {
            return pathoProba;
        }

        boolean isPathogenic() {
            return pathoProba > threshold;
        }
    }

    public static final class Builder {
        private final Map<Culprit, Fragment> data = new HashMap<>();

        private Builder() {
        }

        public Builder setDonorData(double proba, double threshold) {
            this.data.put(Culprit.DONOR, new Fragment(proba, threshold));
            return this;
        }

        public Builder setAcceptorData(double proba, double threshold) {
            this.data.put(Culprit.ACCEPTOR, new Fragment(proba, threshold));
            return this;
        }

        public PredictionImpl build() {
            return new PredictionImpl(this);
        }
    }
}
