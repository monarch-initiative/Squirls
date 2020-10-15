package org.monarchinitiative.squirls.core;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class StandardPrediction implements Prediction {

    /**
     * List of pairs of prediction & thresholds.
     */
    protected final Set<PartialPrediction> partialPredictions;

    private StandardPrediction(Set<PartialPrediction> partialPredictions) {
        this.partialPredictions = partialPredictions;
    }


    public static StandardPrediction of(PartialPrediction... partialPrediction) {
        return new StandardPrediction(Set.of(partialPrediction));
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

}
