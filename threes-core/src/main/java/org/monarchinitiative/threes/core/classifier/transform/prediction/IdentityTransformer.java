package org.monarchinitiative.threes.core.classifier.transform.prediction;

import org.monarchinitiative.threes.core.classifier.Prediction;

/**
 * This prediction transformer has the purpose of an identity function. It does not perform any transformation but
 * returns the original prediction.
 */
public class IdentityTransformer implements PredictionTransformer {

    private IdentityTransformer() {
        // private no-op
    }

    public static IdentityTransformer getInstance() {
        return new IdentityTransformer();
    }

    @Override
    public Prediction transform(Prediction prediction) {
        return prediction;
    }
}
