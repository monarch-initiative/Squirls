package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;

// TODO - rename once we have a name
public interface OverlordClassifier extends Classifier<FeatureData> {

    int predict(FeatureData instance);

    DoubleMatrix predictProba(FeatureData instance);
}
