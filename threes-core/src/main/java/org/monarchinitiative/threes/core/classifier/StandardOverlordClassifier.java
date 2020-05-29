package org.monarchinitiative.threes.core.classifier;

import com.google.common.collect.Sets;
import org.monarchinitiative.threes.core.classifier.forest.RandomForest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This classifier consists of two {@link Pipeline}s, each pipeline consists of an imputer and
 * a {@link RandomForest} classifier. Together they are used to make predictions.
 * <p>
 * For each pipeline, there are separate thresholds applied when making variant classification.
 */
public class StandardOverlordClassifier implements OverlordClassifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardOverlordClassifier.class);

    private final BinaryClassifier<FeatureData> donorClf, acceptorClf;

    private final Set<String> usedFeatures;

    private final Double donorThreshold, acceptorThreshold;

    private StandardOverlordClassifier(Builder builder) {
        donorClf = Objects.requireNonNull(builder.donorClf, "Donor classifier cannot be null");
        acceptorClf = Objects.requireNonNull(builder.acceptorClf, "Acceptor classifier cannot be null");
        if (builder.donorThreshold == null || builder.donorThreshold.isNaN()) {
            throw new IllegalArgumentException("donor threshold must be specified");
        }
        donorThreshold = builder.donorThreshold;

        if (builder.acceptorThreshold == null || builder.acceptorThreshold.isNaN()) {
            throw new IllegalArgumentException("acceptor threshold must be specified");
        }
        acceptorThreshold = builder.acceptorThreshold;

        usedFeatures = Stream.concat(donorClf.usedFeatureNames().stream(), acceptorClf.usedFeatureNames().stream())
                .collect(Collectors.toSet());
        LOGGER.debug("initialized classifier with the following features: {}",
                usedFeatures.stream().sorted().collect(Collectors.joining(", ", "[", "]")));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> usedFeatureNames() {
        return usedFeatures;
    }

    /**
     * Predict class label for given instance. The instance should contain all features that are required by
     * {@link #usedFeatureNames()}.
     *
     * @param instance instance used for prediction
     * @return class label
     * @throws PredictionException if a required feature is missing or if if there are any other problems in the
     *                             prediction process
     */
    @Override
    public Prediction predict(FeatureData instance) throws PredictionException {
        if (!instance.getFeatureNames().containsAll(usedFeatures)) {
            throw new PredictionException(String.format("Missing one or more required features `%s`",
                    Sets.difference(usedFeatures, instance.getFeatureNames()).stream()
                            .collect(Collectors.joining(",", "[", "]"))));
        }

        final double donorProba = donorClf.predictProba(instance);
        final double acceptorProba = acceptorClf.predictProba(instance);


        return StandardPrediction.builder()
                .addProbaThresholdPair(donorProba, donorThreshold)
                .addProbaThresholdPair(acceptorProba, acceptorThreshold)
                .build();
    }

    public static final class Builder {
        private BinaryClassifier<FeatureData> donorClf;
        private BinaryClassifier<FeatureData> acceptorClf;
        private Double donorThreshold = Double.NaN;
        private Double acceptorThreshold = Double.NaN;

        private Builder() {
        }

        public Builder donorClf(BinaryClassifier<FeatureData> donorClf) {
            this.donorClf = donorClf;
            return this;
        }

        public Builder acceptorClf(BinaryClassifier<FeatureData> acceptorClf) {
            this.acceptorClf = acceptorClf;
            return this;
        }

        public Builder donorThreshold(double donorThreshold) {
            this.donorThreshold = donorThreshold;
            return this;
        }

        public Builder acceptorThreshold(double acceptorThreshold) {
            this.acceptorThreshold = acceptorThreshold;
            return this;
        }

        public StandardOverlordClassifier build() {
            return new StandardOverlordClassifier(this);
        }
    }
}
