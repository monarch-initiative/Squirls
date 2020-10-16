package org.monarchinitiative.squirls.core.classifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StandardSquirlsClassifier implements SquirlsClassifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardSquirlsClassifier.class);

    private static final AtomicBoolean MISSING_FEATURE_REPORTED = new AtomicBoolean(false);

    private final ThresholdingBinaryClassifier<Classifiable> donorClf, acceptorClf;

    private final Set<String> usedFeatures;

    private StandardSquirlsClassifier(ThresholdingBinaryClassifier<Classifiable> donorClf,
                                      ThresholdingBinaryClassifier<Classifiable> acceptorClf) {
        this.donorClf = Objects.requireNonNull(donorClf, "Donor classifier cannot be null");
        this.acceptorClf = Objects.requireNonNull(acceptorClf, "Acceptor classifier cannot be null");

        usedFeatures = Stream.concat(donorClf.usedFeatureNames().stream(), acceptorClf.usedFeatureNames().stream())
                .collect(Collectors.toUnmodifiableSet());
        LOGGER.debug("Initialized classifier with the following features: {}",
                usedFeatures.stream().sorted().collect(Collectors.joining(", ", "[", "]")));
    }

    public static StandardSquirlsClassifier of(ThresholdingBinaryClassifier<Classifiable> donorClf, ThresholdingBinaryClassifier<Classifiable> acceptorClf) {
        return new StandardSquirlsClassifier(donorClf, acceptorClf);
    }

    @Override
    public Set<String> usedFeatureNames() {
        return usedFeatures;
    }

    @Override
    public <T extends Classifiable> T predict(T data) {
        if (data.getFeatureNames().containsAll(usedFeatures)) {
            // we have all the features we need for making a prediction here
            try {
                data.setPrediction(StandardPrediction.of(donorClf.runPrediction(data), acceptorClf.runPrediction(data)));
            } catch (PredictionException e) {
                LOGGER.debug("Error: ", e);
                data.setPrediction(Prediction.emptyPrediction());
            }
        } else {
            // at least one from the required features is missing. Let's report that, but only once, in order not to
            // flood the console
            if (MISSING_FEATURE_REPORTED.compareAndExchange(false, true)) {
                Set<String> difference = usedFeatures.stream()
                        .filter(fname -> !data.getFeatureNames()
                                .contains(fname)).collect(Collectors.toSet());
                // report the error
                String errorMsg = String.format("Missing one or more required features `[%s]`",
                        String.join(",", difference));
                LOGGER.warn(errorMsg);
            }
            data.setPrediction(EmptyPrediction.getInstance());
        }

        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardSquirlsClassifier that = (StandardSquirlsClassifier) o;
        return Objects.equals(donorClf, that.donorClf) &&
                Objects.equals(acceptorClf, that.acceptorClf) &&
                Objects.equals(usedFeatures, that.usedFeatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donorClf, acceptorClf, usedFeatures);
    }

    @Override
    public String toString() {
        return "StandardSquirlsClassifier{" +
                "donorClf=" + donorClf +
                ", acceptorClf=" + acceptorClf +
                ", usedFeatures=" + usedFeatures +
                '}';
    }
}
