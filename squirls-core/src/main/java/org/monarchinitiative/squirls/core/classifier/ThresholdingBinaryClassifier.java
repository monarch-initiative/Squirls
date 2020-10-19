package org.monarchinitiative.squirls.core.classifier;

import java.util.Objects;
import java.util.Set;


public class ThresholdingBinaryClassifier<T extends Classifiable> {

    private final BinaryClassifier<T> classifier;

    private final double threshold;

    private ThresholdingBinaryClassifier(BinaryClassifier<T> classifier, double threshold) {
        this.classifier = Objects.requireNonNull(classifier, "Classifier cannot be null");
        this.threshold = requireNonNan(threshold);
    }

    public static <T extends Classifiable> ThresholdingBinaryClassifier<T> of(BinaryClassifier<T> classifier, double threshold) {
        return new ThresholdingBinaryClassifier<>(classifier, threshold);
    }

    private static double requireNonNan(double threshold) {
        if (Double.isNaN(threshold)) {
            throw new IllegalArgumentException("Threshold cannot be NaN");
        }
        return threshold;
    }

    public Set<String> usedFeatureNames() {
        return classifier.usedFeatureNames();
    }

    public PartialPrediction runPrediction(T data) throws PredictionException {
        return PartialPrediction.of(classifier.getName(), classifier.predictProba(data), threshold);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThresholdingBinaryClassifier<?> that = (ThresholdingBinaryClassifier<?>) o;
        return Double.compare(that.threshold, threshold) == 0 &&
                Objects.equals(classifier, that.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classifier, threshold);
    }

    @Override
    public String toString() {
        return "ThresholdingBinaryClassifier{" +
                "classifier=" + classifier +
                ", threshold=" + threshold +
                '}';
    }
}
