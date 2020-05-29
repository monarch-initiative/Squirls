package org.monarchinitiative.threes.core.classifier;

import java.util.Objects;

/**
 * This class predicts pathogenicity if prediction obtained by any decision function exceeds given threshold.
 * <p>
 * Pathogenicity probability is simply the highest probability value that is above given threshold.
 */
public class SimplePrediction extends AbstractPrediction {

    private SimplePrediction(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
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
        SimplePrediction that = (SimplePrediction) o;
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

    public static class Builder extends AbstractPrediction.Builder<Builder> {

        private Builder() {
            // private no-op
        }

        @Override
        public SimplePrediction build() {
            return new SimplePrediction(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
