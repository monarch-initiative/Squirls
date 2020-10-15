package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class transforms predictions made by donor and acceptor site-specific models into a single pathogenicity
 * score in range [0,1].
 * <p>
 * Full logistic regression is used to perform the transformation.
 */
public class RegularLogisticRegression implements PredictionTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegularLogisticRegression.class);

    /**
     * A flag to make sure that missing feature is reported only once and that it does not flood the console.
     */
    private final AtomicBoolean reportMissingPrediction = new AtomicBoolean(true);
    private final double donorSlope, acceptorSlope, intercept;

    private RegularLogisticRegression(double donorSlope, double acceptorSlope, double intercept) {
        this.donorSlope = donorSlope;
        this.acceptorSlope = acceptorSlope;
        this.intercept = intercept;
    }

    public static RegularLogisticRegression getInstance(double donorSlope, double acceptorSlope, double intercept) {
        return new RegularLogisticRegression(donorSlope, acceptorSlope, intercept);
    }

    public double getDonorSlope() {
        return donorSlope;
    }

    public double getAcceptorSlope() {
        return acceptorSlope;
    }

    public double getIntercept() {
        return intercept;
    }

    @Override
    public String getName() {
        return "regular_logistic_regression";
    }

    @Override
    public <T extends MutablePrediction> T transform(T data) {
        final Map<String, Prediction.PartialPrediction> predictions = data.getPrediction().getPartialPredictions().stream()
                .collect(Collectors.toMap(Prediction.PartialPrediction::getName, Function.identity()));

        // this currently matches the strings set to Pipelines when deserializing donor and acceptor pipelines
        if (!predictions.containsKey("donor") || !predictions.containsKey("acceptor")) {
            // cannot perform transformation
            if (reportMissingPrediction.compareAndSet(true, false)) {
                LOGGER.warn("Missing prediction for `donor` or `acceptor` site in `{}`. Other missing predictions will not be reported.", data);
            }
            return data;
        }

        final Prediction transformed = transform(predictions.get("donor"), predictions.get("acceptor"));

        data.setPrediction(transformed);
        return data;
    }

    private Prediction transform(Prediction.PartialPrediction donor, Prediction.PartialPrediction acceptor) {
        // scale the pathogenicity
        double patho = logistic(donor.getPathoProba(), acceptor.getPathoProba());

        // then scale the threshold
        double threshold = logistic(donor.getThreshold(), acceptor.getThreshold());
        return StandardPrediction.of(Prediction.PartialPrediction.of(getName(), patho, threshold));
    }

    private double logistic(double donor, double acceptor) {
        double score = Math.exp(-(donorSlope * donor + acceptorSlope * acceptor + intercept));
        return 1 / (1 + score);
    }
}
