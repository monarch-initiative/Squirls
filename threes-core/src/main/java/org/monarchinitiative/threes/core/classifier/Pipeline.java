package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.classifier.forest.RandomForest;
import org.monarchinitiative.threes.core.classifier.impute.MedianImputer;

public class Pipeline implements Classifier<FeatureData> {

    private final MedianImputer imputer;

    private final RandomForest<FeatureData> randomForest;

    public Pipeline(MedianImputer imputer, RandomForest<FeatureData> randomForest) {
        this.imputer = imputer;
        this.randomForest = randomForest;
    }

    @Override
    public int predict(FeatureData instance) {
        final FeatureData transformed = this.imputer.transform().apply(instance);
        return randomForest.predict(transformed);
    }

    @Override
    public DoubleMatrix predictProba(FeatureData instance) {
        final FeatureData transformed = this.imputer.transform().apply(instance);
        return randomForest.predictProba(transformed);
    }

}
