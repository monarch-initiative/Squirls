package org.monarchinitiative.squirls.core;

import java.util.Collection;
import java.util.Collections;

/**
 * Class representing N/A prediction.
 */
class EmptyPrediction implements Prediction {

    private static final EmptyPrediction INSTANCE = new EmptyPrediction();

    private EmptyPrediction() {
        // private no-op
    }


    static EmptyPrediction getInstance() {
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
