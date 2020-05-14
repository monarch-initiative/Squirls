package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.classifier.forest.RandomForest;

import java.util.Objects;

/**
 * This classifier consists of two {@link RandomForest} classifiers which are used to make a prediction for each
 * {@link FeatureData} instance.
 */
// TODO - rename
public class SimpleOverlordClassifier implements OverlordClassifier {

    private final RandomForest<FeatureData> donorClf, acceptorClf;

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
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int predict(FeatureData instance) {
        final DoubleMatrix donorProba = donorClf.predictProba(instance);
        final DoubleMatrix acceptorProba = acceptorClf.predictProba(instance);

        final int isDonorPathogenic = donorProba.get(0, 1) < donorThreshold ? 0 : 1;
        final int isAcceptorPathogenic = acceptorProba.get(0, 1) < acceptorThreshold ? 0 : 1;

        return Math.max(isDonorPathogenic, isAcceptorPathogenic);
    }

    @Override
    public DoubleMatrix predictProba(FeatureData instance) {
        final DoubleMatrix donorProba = donorClf.predictProba(instance);
        final DoubleMatrix acceptorProba = acceptorClf.predictProba(instance);

        final double pathoProba = Math.max(donorProba.get(0, 1), acceptorProba.get(0, 1));

        return new DoubleMatrix(1, 2, 1 - pathoProba, pathoProba);
    }

    public static final class Builder {
        private RandomForest<FeatureData> donorClf;
        private RandomForest<FeatureData> acceptorClf;
        private Double donorThreshold = Double.NaN;
        private Double acceptorThreshold = Double.NaN;

        private Builder() {
        }

        public Builder donorClf(RandomForest<FeatureData> donorClf) {
            this.donorClf = donorClf;
            return this;
        }

        public Builder acceptorClf(RandomForest<FeatureData> acceptorClf) {
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
