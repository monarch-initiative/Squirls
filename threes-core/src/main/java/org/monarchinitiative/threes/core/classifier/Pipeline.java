package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Pipeline class inspired by scikit-learn. This pipeline consists of an imputer followed by a classifier.
 */
public class Pipeline<T extends FeatureData> implements Classifier<T> {

    private final FeatureTransformer<T> transformer;

    private final Classifier<T> classifier;

    private Pipeline(Builder<T> builder) {
        transformer = builder.transformer;
        classifier = builder.randomForest;
    }

    public static <T extends FeatureData> Builder<T> builder() {
        return new Builder<>();
    }


    @Override
    public Set<String> usedFeatureNames() {
        return Stream.concat(transformer.usedFeatureNames().stream(), classifier.usedFeatureNames().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public int predict(T instance) throws PredictionException {
        final T transformed = this.transformer.transform(instance);
        return classifier.predict(transformed);
    }

    @Override
    public DoubleMatrix predictProba(T instance) throws PredictionException {
        final T transformed = this.transformer.transform(instance);
        return classifier.predictProba(transformed);
    }

    public static final class Builder<T extends FeatureData> {
        private FeatureTransformer<T> transformer;
        private Classifier<T> randomForest;

        private Builder() {
        }

        public Builder<T> transformer(FeatureTransformer<T> transformer) {
            this.transformer = transformer;
            return this;
        }

        public Builder<T> classifier(Classifier<T> randomForest) {
            this.randomForest = randomForest;
            return this;
        }

        public Pipeline<T> build() {
            return new Pipeline<>(this);
        }
    }
}
