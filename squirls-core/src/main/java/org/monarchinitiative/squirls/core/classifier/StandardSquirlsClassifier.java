package org.monarchinitiative.squirls.core.classifier;

import com.google.common.collect.Sets;
import org.monarchinitiative.squirls.core.Prediction;
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

    private final BinaryClassifier<Classifiable> donorClf, acceptorClf;

    private final Set<String> usedFeatures;

    private final Double donorThreshold, acceptorThreshold;

    public StandardSquirlsClassifier(Builder builder) {
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
                .collect(Collectors.toUnmodifiableSet());
        LOGGER.debug("initialized classifier with the following features: {}",
                usedFeatures.stream().sorted().collect(Collectors.joining(", ", "[", "]")));
    }


    public static Builder builder() {
        return new Builder();
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
                final double donorProba = donorClf.predictProba(data);
                final double acceptorProba = acceptorClf.predictProba(data);
                data.setPrediction(StandardPrediction.builder()
                        .addProbaThresholdPair(donorClf.getName(), donorProba, donorThreshold)
                        .addProbaThresholdPair(acceptorClf.getName(), acceptorProba, acceptorThreshold)
                        .build());
            } catch (PredictionException e) {
                LOGGER.debug("Error: ", e);
                data.setPrediction(Prediction.emptyPrediction());
            }
        } else {
            // at least one from the required features is missing. Let's report that, but only once, in order not to
            // flood the console
            if (MISSING_FEATURE_REPORTED.compareAndExchange(false, true)) {
                // report the error
                String errorMsg = String.format("Missing one or more required features `%s`",
                        Sets.difference(usedFeatures, data.getFeatureNames()).stream()
                                .collect(Collectors.joining(",", "[", "]")));
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
                Objects.equals(usedFeatures, that.usedFeatures) &&
                Objects.equals(donorThreshold, that.donorThreshold) &&
                Objects.equals(acceptorThreshold, that.acceptorThreshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donorClf, acceptorClf, usedFeatures, donorThreshold, acceptorThreshold);
    }

    @Override
    public String toString() {
        return "StandardSquirlsClassifier{" +
                "donorClf=" + donorClf +
                ", acceptorClf=" + acceptorClf +
                ", usedFeatures=" + usedFeatures +
                ", donorThreshold=" + donorThreshold +
                ", acceptorThreshold=" + acceptorThreshold +
                '}';
    }

    public static final class Builder {
        private BinaryClassifier<Classifiable> donorClf;
        private BinaryClassifier<Classifiable> acceptorClf;
        private Double donorThreshold = Double.NaN;
        private Double acceptorThreshold = Double.NaN;

        private Builder() {
        }

        public Builder donorClf(BinaryClassifier<Classifiable> donorClf) {
            this.donorClf = donorClf;
            return this;
        }

        public Builder acceptorClf(BinaryClassifier<Classifiable> acceptorClf) {
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

        public StandardSquirlsClassifier build() {
            return new StandardSquirlsClassifier(this);
        }
    }
}
