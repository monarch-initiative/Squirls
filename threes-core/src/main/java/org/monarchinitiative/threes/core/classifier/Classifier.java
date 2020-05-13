package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;

public interface Classifier<T> {

    int predict(T instance);

    DoubleMatrix predictProba(T instance);
}
