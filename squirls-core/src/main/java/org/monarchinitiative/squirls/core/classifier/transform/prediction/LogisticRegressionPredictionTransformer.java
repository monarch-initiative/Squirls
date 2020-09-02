package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;

import java.util.Objects;

/**
 * This class transforms pathogenicity probabilities using logistic regression parameters.
 */
public class LogisticRegressionPredictionTransformer implements PredictionTransformer {

    private final double slope;
    private final double intercept;

    private LogisticRegressionPredictionTransformer(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public static LogisticRegressionPredictionTransformer getInstance(double slope, double intercept) {
        return new LogisticRegressionPredictionTransformer(slope, intercept);
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

        // this check should not be necessary
        //   double score = 1 / (1 + exp);
        //   return Math.max(0., Math.min(1., score));

        return 1 / (1 + exp);
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }


    @Override
    public <T extends MutablePrediction> T transform(T data) {
        final StandardPrediction.Builder builder = StandardPrediction.builder();
        for (Prediction.PartialPrediction partialPrediction : data.getPrediction().getPartialPredictions()) {
            final double pathoProba = transform(partialPrediction.getPathoProba(), slope, intercept);
            final double threshold = transform(partialPrediction.getThreshold(), slope, intercept);
            builder.addProbaThresholdPair(pathoProba, threshold);
        }

        data.setPrediction(builder.build());

        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogisticRegressionPredictionTransformer that = (LogisticRegressionPredictionTransformer) o;
        return Double.compare(that.slope, slope) == 0 &&
                Double.compare(that.intercept, intercept) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slope, intercept);
    }

    @Override
    public String toString() {
        return "LogisticRegressionPredictionTransformer{" +
                "slope=" + slope +
                ", intercept=" + intercept +
                '}';
    }
}
