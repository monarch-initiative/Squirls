package org.monarchinitiative.squirls.core.classifier.transform.prediction;


/**
 * This prediction transformer has the purpose of an identity function. It does not perform any transformation but
 * returns the original prediction.
 */
public class IdentityTransformer implements PredictionTransformer {

    private static final IdentityTransformer INSTANCE = new IdentityTransformer();

    private IdentityTransformer() {
        // private no-op
    }

    public static IdentityTransformer getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends MutablePrediction> T transform(T data) {
        return data;
    }
}
