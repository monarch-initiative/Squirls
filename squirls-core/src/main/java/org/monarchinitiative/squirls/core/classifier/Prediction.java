package org.monarchinitiative.squirls.core.classifier;

import java.util.Collection;

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
        double max = Double.NaN;
        for (PartialPrediction pp : getPartialPredictions()) {
            final double proba = pp.getPathoProba();
            if (Double.isNaN(max)) {
                max = proba;
            } else {
                if (max < proba) {
                    max = proba;
                }
            }
        }
        return max;
    }

}
