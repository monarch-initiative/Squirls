package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.monarchinitiative.squirls.core.classifier.PartialPrediction;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;

import java.util.Iterator;
import java.util.Objects;

/**
 * This class transforms pathogenicity probabilities using logistic regression parameters.
 * <p>
 * The <em>simple</em> denotes that slope is only a scalar value and not a vector.
 *
 * @see PredictionTransformer
 * @see RegularLogisticRegression
 */
@Deprecated // in favor of RegularLogisticRegression
public class SimpleLogisticRegression implements PredictionTransformer {

    private final double slope;
    private final double intercept;

    private SimpleLogisticRegression(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public static SimpleLogisticRegression getInstance(double slope, double intercept) {
        return new SimpleLogisticRegression(slope, intercept);
    }

    /**
     * Logistic regression in small scale. Transform <code>x</code> by applying <code>slope</code> and
     * <code>intercept</code>, then scale with sigmoid function.
     *
     * @param x probability value about to be transformed, expecting value in range [0,1]
     * @return transformed probability value clipped to be in range [0,1] if necessary
     */
    static double transform(double x, double slope, double intercept) {
        // apply the logistic regression transformation
        final double exp = Math.exp(-(slope * x + intercept));

        return 1 / (1 + exp);
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    @Override
    public String getName() {
        return "simple_logistic_regression";
    }

    @Override
    public <T extends MutablePrediction> T transform(T data) {
        PartialPrediction[] predictions = new PartialPrediction[data.getPrediction().getPartialPredictions().size()];

        final Iterator<PartialPrediction> iterator = data.getPrediction().getPartialPredictions().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            final PartialPrediction pp = iterator.next();
            final PartialPrediction transformed = PartialPrediction.of(getName(),
                    transform(pp.getPathoProba(), slope, intercept),
                    transform(pp.getThreshold(), slope, intercept));
            predictions[i] = transformed;
            i++;
        }
        data.setPrediction(StandardPrediction.of(predictions));

        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLogisticRegression that = (SimpleLogisticRegression) o;
        return Double.compare(that.slope, slope) == 0 &&
                Double.compare(that.intercept, intercept) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slope, intercept);
    }

    @Override
    public String toString() {
        return "SimpleLogisticRegression{" +
                "slope=" + slope +
                ", intercept=" + intercept +
                '}';
    }
}
