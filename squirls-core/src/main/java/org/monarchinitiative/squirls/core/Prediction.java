package org.monarchinitiative.squirls.core;

import org.monarchinitiative.squirls.core.classifier.BinaryClassifier;
import org.monarchinitiative.squirls.core.classifier.EmptyPrediction;

import java.util.Collection;
import java.util.Objects;

/**
 * The implementing classes represent predictions made by the {@link BinaryClassifier} with respect
 * to a single transcript.
 */
public interface Prediction {

    static Prediction emptyPrediction() {
        return EmptyPrediction.getInstance();
    }

    /**
     * Predictions are being made by one or more decision functions, where {@link PartialPrediction} represents outcome
     * of a single decision function.
     *
     * @return a collection of partial predictions
     */
    Collection<PartialPrediction> getPartialPredictions();

    /**
     * @return <code>true</code> if binary classifier considers this {@link Prediction} to be positive
     */
    boolean isPositive();

    /**
     * @return the maximum pathogenicity prediction value
     */
    default double getMaxPathogenicity() {
        return getPartialPredictions().stream()
                .mapToDouble(PartialPrediction::getPathoProba)
                .max()
                .orElse(Double.NaN);
    }

    /**
     * This class represents a fragment of information from the decision function, a single prediction of an ensemble
     * which calculated pathogenicity probability.
     */
    class PartialPrediction {

        private final String name;
        private final double pathoProba;
        private final double threshold;

        private PartialPrediction(String name, double pathoProba, double threshold) {
            this.name = name;
            this.pathoProba = pathoProba;
            this.threshold = threshold;
        }

        public static PartialPrediction of(String name, double pathoProba, double threshold) {
            return new PartialPrediction(name, pathoProba, threshold);
        }

        public String getName() {
            return name;
        }

        public double getThreshold() {
            return threshold;
        }

        public double getPathoProba() {
            return pathoProba;
        }

        public boolean isPathogenic() {
            return pathoProba > threshold;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PartialPrediction that = (PartialPrediction) o;
            return Double.compare(that.pathoProba, pathoProba) == 0 &&
                    Double.compare(that.threshold, threshold) == 0 &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, pathoProba, threshold);
        }

        @Override
        public String toString() {
            return "PartialPrediction{" +
                    "name='" + name + '\'' +
                    ", pathoProba=" + pathoProba +
                    ", threshold=" + threshold +
                    '}';
        }
    }
}
