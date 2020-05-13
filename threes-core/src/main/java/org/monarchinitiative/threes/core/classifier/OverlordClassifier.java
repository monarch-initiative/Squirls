package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;

public interface OverlordClassifier extends Classifier<FeatureData> {

    int predict(FeatureData instance);

    DoubleMatrix predictProba(FeatureData instance);
}
