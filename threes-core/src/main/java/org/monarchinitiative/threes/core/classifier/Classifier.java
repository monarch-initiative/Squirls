package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;

public interface Classifier<T extends FeatureData> {

    int predict(T instance);

    DoubleMatrix predictProba(T instance);
}
