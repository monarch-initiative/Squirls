package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.Prediction;

import java.util.Collection;
import java.util.Collections;

/**
 * Class representing N/A prediction.
 */
public class EmptyPrediction implements Prediction {

    private static final EmptyPrediction INSTANCE = new EmptyPrediction();

    private EmptyPrediction() {
        // private no-op
    }


    public static EmptyPrediction getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<PartialPrediction> getPartialPredictions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isPositive() {
        return false;
    }
}
