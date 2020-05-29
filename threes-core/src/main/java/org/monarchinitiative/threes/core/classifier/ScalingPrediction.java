package org.monarchinitiative.threes.core.classifier;

import java.util.OptionalDouble;

/**
 * This prediction applies logistic regression transformation to the reported pathogenicity probability. Parameters of
 * the transformation were learnt during model training.
 */
public class ScalingPrediction extends AbstractPrediction {

    /**
     * The slope parameter of logistic regression transformation being performed here.
     */
    private final double slope;

    /**
     * The intercept parameter of logistic regression transformation being performed here.
     */
    private final double intercept;

    private ScalingPrediction(Builder builder) {
        super(builder);
        this.intercept = builder.intercept;
        this.slope = builder.slope;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public double getPathoProba() {
        OptionalDouble pathogenicityOpt = data.values().stream()
                .mapToDouble(Fragment::getPathoProba)
                .max();
        return pathogenicityOpt.isPresent() ? transform(pathogenicityOpt.getAsDouble()) : 0.;
    }

    /**
     * Logistic regression in small scale. Apply <code>slope</code> and <code>intercept</code>, then scale with
     * sigmoid function.
     *
     * @param x probability value about to be transformed, expecting value in range [0,1]
     * @return transformed probability value clipped to be in range [0,1] if necessary
     */
    double transform(double x) {
        // apply the logistic regression transformation
        final double exp = Math.exp(-(slope * x + intercept));
        double score = 1 / (1 + exp);

        // make sure we stay between 0.0 and 1.0
        return Math.max(0., Math.min(1., score));
    }

    public static class Builder extends AbstractPrediction.Builder<Builder> {

        private double intercept, slope;

        private Builder() {
            // private no-op
        }


        public Builder intercept(double intercept) {
            this.intercept = intercept;
            return self();
        }

        public Builder slope(double slope) {
            this.slope = slope;
            return self();
        }

        public ScalingPrediction build() {
            return new ScalingPrediction(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
