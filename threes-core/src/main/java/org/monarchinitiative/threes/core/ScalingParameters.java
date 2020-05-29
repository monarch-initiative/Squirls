package org.monarchinitiative.threes.core;

import java.util.Objects;

public class ScalingParameters {

    private final double slope;
    private final double intercept;
    private final double threshold;

    private ScalingParameters(Builder builder) {
        slope = builder.slope;
        intercept = builder.intercept;
        threshold = builder.threshold;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ScalingParameters defaultParameters() {
        return builder()
                .slope(1.)
                .intercept(0.)
                .threshold(.5)
                .build();
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public double getThreshold() {
        return threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScalingParameters that = (ScalingParameters) o;
        return Double.compare(that.slope, slope) == 0 &&
                Double.compare(that.intercept, intercept) == 0 &&
                Double.compare(that.threshold, threshold) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slope, intercept, threshold);
    }

    public static final class Builder {
        private double slope;
        private double intercept;
        private double threshold;

        private Builder() {
        }

        public Builder slope(double slope) {
            this.slope = slope;
            return this;
        }

        public Builder intercept(double intercept) {
            this.intercept = intercept;
            return this;
        }

        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public ScalingParameters build() {
            return new ScalingParameters(this);
        }
    }
}
