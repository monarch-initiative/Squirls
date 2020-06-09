package org.monarchinitiative.threes.core.classifier.transform.prediction;

import org.monarchinitiative.threes.core.Prediction;
import org.monarchinitiative.threes.core.classifier.StandardPrediction;

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
        double score = 1 / (1 + exp);

        // make sure we stay between 0.0 and 1.0
        return Math.max(0., Math.min(1., score));
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
}
