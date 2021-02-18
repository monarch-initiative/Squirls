package org.monarchinitiative.squirls.io.classifier.v046;

import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.classifier.BinaryClassifier;
import org.monarchinitiative.squirls.core.classifier.PredictionException;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.SquirlsFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Daniel Danis
 */
class SquirlsClassifierV046 implements SquirlsClassifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirlsClassifierV046.class);

    private static final AtomicBoolean MISSING_FEATURE_REPORTED = new AtomicBoolean(false);

    private static final int EXPECTED_NUMBER_OF_FEATURES = 2; // corresponds to donor and acceptor output

    private final BinaryClassifier<SquirlsFeatures> donorClf, acceptorClf;

    private final List<Double> means;
    private final List<Double> stds;

    private final List<Double> coef;

    private final double threshold;

    private final Set<String> usedFeatures;

    /*
    Instantiate in SquirlsClassifierDeserializerV046
     */
    SquirlsClassifierV046(BinaryClassifier<SquirlsFeatures> donorClf,
                          BinaryClassifier<SquirlsFeatures> acceptorClf,
                          List<Double> means,
                          List<Double> variances,
                          List<Double> coef,
                          double intercept,
                          double threshold) {
        this.donorClf = Objects.requireNonNull(donorClf, "Donor classifier cannot be null");
        this.acceptorClf = Objects.requireNonNull(acceptorClf, "Acceptor classifier cannot be null");

        usedFeatures = Stream.concat(donorClf.usedFeatureNames().stream(), acceptorClf.usedFeatureNames().stream())
                .collect(Collectors.toUnmodifiableSet());
        LOGGER.debug("Initialized classifier with the following features: {}",
                usedFeatures.stream().sorted().collect(Collectors.joining(", ", "[", "]")));

        this.means = Objects.requireNonNull(means, "Means cannot be null");
        this.stds = mapVarianceToStd(Objects.requireNonNull(variances, "Variances cannot be null"));


        if (means.size() != EXPECTED_NUMBER_OF_FEATURES || coef.size() != EXPECTED_NUMBER_OF_FEATURES) {
            throw new IllegalArgumentException("Number of elements in means (" + means.size() +
                    "), variances (" + variances.size() +
                    "), and coefficients (" + coef.size() + ") arguments is not equal to the expected " +
                    EXPECTED_NUMBER_OF_FEATURES);
        }

        this.coef = new ArrayList<>(EXPECTED_NUMBER_OF_FEATURES + 1); // +1 for the bias
        this.coef.add(intercept);
        this.coef.addAll(coef);
        this.threshold = threshold;
    }

    /**
     * Convert list of parameter variances to respective standard deviations.
     *
     * @param variances list of variances as doubles
     * @return list of standard deviations computed from <code>variances</code>
     */
    private static List<Double> mapVarianceToStd(List<Double> variances) {
        List<Double> stds = new ArrayList<>(variances.size());
        for (Double variance : variances) {
            stds.add(Math.sqrt(variance));
        }
        return stds;
    }

    @Override
    public Prediction predict(SquirlsFeatures data) {
        if (data.getFeatureNames().containsAll(usedFeatures)) {
            // we have all the features we need for making a prediction here
            try {
                List<Double> siteSpecificProbas = List.of(donorClf.predictProba(data), acceptorClf.predictProba(data));

                // standard scaling - center the input by subtracting the mean and dividing by standard deviation
                double[] centered = new double[siteSpecificProbas.size() + 1]; // +1 for bias
                centered[0] = 1;
                for (int i = 0; i < siteSpecificProbas.size(); i++) {
                    centered[i + 1] = (siteSpecificProbas.get(i) - means.get(i)) / stds.get(i);
                }

                // logistic regression
                double dotSum = 0.;
                for (int i = 0; i < centered.length; i++) {
                    dotSum += centered[i] * coef.get(i);
                }

                double pathogenicity = 1 / (1 + Math.exp(-dotSum));

                return Prediction.of("standard_scaler__logistic_regression", pathogenicity, threshold);
            } catch (PredictionException e) {
                LOGGER.debug("Error: ", e);
                return Prediction.emptyPrediction();
            }
        } else {
            // at least one from the required features is missing. Let's report that, but only once, in order not to
            // flood the console
            if (!MISSING_FEATURE_REPORTED.compareAndExchange(false, true)) {
                Set<String> difference = usedFeatures.stream()
                        .filter(featureName -> !data.getFeatureNames().contains(featureName))
                        .collect(Collectors.toSet());
                // report the error
                String errorMsg = String.format("Missing one or more required features `[%s]`",
                        String.join(",", difference));
                LOGGER.warn(errorMsg);
            }
            return Prediction.emptyPrediction();
        }
    }

}
