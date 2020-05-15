package org.monarchinitiative.threes.core.classifier;

import com.google.common.collect.Sets;
import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.classifier.forest.RandomForest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This classifier consists of two {@link Pipeline}s, each pipeline consists of an imputer and
 * a {@link RandomForest} classifier. Together they are used to make predictions.
 * <p>
 * For each pipeline, there are separate thresholds applied when making variant classification.
 */
public class SimpleOverlordClassifier implements OverlordClassifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleOverlordClassifier.class);

    private final Classifier<FeatureData> donorClf, acceptorClf;

    private final Set<String> requiredFeatures;

    private final Double donorThreshold, acceptorThreshold;

    private SimpleOverlordClassifier(Builder builder) {
        donorClf = Objects.requireNonNull(builder.donorClf, "Donor classifier cannot be null");
        acceptorClf = Objects.requireNonNull(builder.acceptorClf, "Acceptor classifier cannot be null");
        if (builder.donorThreshold.isNaN()) {
            throw new IllegalArgumentException("donor threshold cannot be NaN");
        }
        donorThreshold = builder.donorThreshold;

        if (builder.acceptorThreshold.isNaN()) {
            throw new IllegalArgumentException("acceptor threshold cannot be NaN");
        }
        acceptorThreshold = builder.acceptorThreshold;

        requiredFeatures = Set.copyOf(builder.requiredFeatures);
        LOGGER.info("initialized classifier that uses the following features: `{}`",
                requiredFeatures.stream().collect(Collectors.joining(",", "[", "]")));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> usedFeatureNames() {
        return null;
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
    public int predict(FeatureData instance) throws PredictionException {
        if (!instance.getFeatureNames().containsAll(requiredFeatures)) {
            throw new PredictionException(String.format("Missing one or more required features `%s`",
                    Sets.difference(requiredFeatures, instance.getFeatureNames()).stream()
                            .collect(Collectors.joining(",", "[", "]"))));
        }

        final DoubleMatrix donorProba = donorClf.predictProba(instance);
        final DoubleMatrix acceptorProba = acceptorClf.predictProba(instance);

        final int isDonorPathogenic = donorProba.get(0, 1) < donorThreshold ? 0 : 1;
        final int isAcceptorPathogenic = acceptorProba.get(0, 1) < acceptorThreshold ? 0 : 1;

        return Math.max(isDonorPathogenic, isAcceptorPathogenic);
    }

    /**
     * Predict class probabilities for given instance. The instance should contain all features that are required by
     * {@link #usedFeatureNames()}.
     *
     * @param instance instance used for prediction
     * @return class label
     * @throws PredictionException if a required feature is missing or if if there are any other problems in the
     *                             prediction process
     */
    @Override
    public DoubleMatrix predictProba(FeatureData instance) throws PredictionException {
        final DoubleMatrix donorProba = donorClf.predictProba(instance);
        final DoubleMatrix acceptorProba = acceptorClf.predictProba(instance);

        final DoubleMatrix pathoProba = donorProba.getColumn(1).max(acceptorProba.getColumn(1));
        final DoubleMatrix ones = DoubleMatrix.ones(pathoProba.rows, pathoProba.columns);
        final DoubleMatrix benignProba = ones.sub(pathoProba);

        return DoubleMatrix.concatHorizontally(benignProba, pathoProba);
    }

    public static final class Builder {
        private final Set<String> requiredFeatures = new HashSet<>();
        private Classifier<FeatureData> donorClf;
        private Classifier<FeatureData> acceptorClf;
        private Double donorThreshold = Double.NaN;
        private Double acceptorThreshold = Double.NaN;

        private Builder() {
        }

        public Builder featureNames(Collection<String> featureNames) {
            this.requiredFeatures.addAll(featureNames);
            return this;
        }

        public Builder featureNames(String... featureNames) {
            this.requiredFeatures.addAll(Arrays.asList(featureNames));
            return this;
        }

        public Builder donorClf(Classifier<FeatureData> donorClf) {
            this.donorClf = donorClf;
            return this;
        }

        public Builder acceptorClf(Classifier<FeatureData> acceptorClf) {
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

        public SimpleOverlordClassifier build() {
            return new SimpleOverlordClassifier(this);
        }
    }
}
