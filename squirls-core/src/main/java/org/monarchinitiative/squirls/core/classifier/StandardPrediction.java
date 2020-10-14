package org.monarchinitiative.squirls.core.classifier;

import org.monarchinitiative.squirls.core.Prediction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class StandardPrediction implements Prediction {

    /**
     * List of pairs of prediction & thresholds.
     */
    protected final Set<PartialPrediction> partialPredictions;

    private StandardPrediction(Builder builder) {
        this.partialPredictions = Set.copyOf(builder.scores);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Collection<PartialPrediction> getPartialPredictions() {
        return partialPredictions;
    }

    @Override
    public boolean isPositive() {
        return partialPredictions.stream()
                .anyMatch(PartialPrediction::isPathogenic);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardPrediction that = (StandardPrediction) o;
        return Objects.equals(partialPredictions, that.partialPredictions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partialPredictions);
    }


    @Override
    public String toString() {
        return "StandardPrediction{" +
                "partialPredictions=" + partialPredictions +
                '}';
    }

    public static class Builder {
        protected final Set<PartialPrediction> scores = new HashSet<>();

        private Builder() {
            // private no-op
        }

        public Builder addProbaThresholdPair(String name, double proba, double threshold) {
            scores.add(PartialPrediction.of(name, proba, threshold));
            return this;
        }

        public StandardPrediction build() {
            return new StandardPrediction(this);
        }
    }
}
