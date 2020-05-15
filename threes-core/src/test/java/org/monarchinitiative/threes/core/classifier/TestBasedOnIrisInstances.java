package org.monarchinitiative.threes.core.classifier;

import java.util.Map;

/**
 * This class defines test instances from Iris dataset.
 */
public class TestBasedOnIrisInstances {

    // the first instance of the iris dataset, class 0 (setosa)
    protected final FeatureData setosaOne = makeIrisFeature(5.1, 3.5, 1.4, 0.2);
    // the fifth instance of the iris dataset, class 0 (setosa)
    protected final FeatureData setosaFive = makeIrisFeature(5., 3.6, 1.4, .2);
    // the fifty-first instance of the iris dataset, class 1 (versicolor)
    protected final FeatureData versicolorOne = makeIrisFeature(7., 3.2, 4.7, 1.4);
    // the fifty-fifth instance of the iris dataset, class 1 (versicolor)
    protected final FeatureData versicolorFive = makeIrisFeature(6.5, 2.8, 4.6, 1.5);
    // the 101st instance of the iris dataset, class 2 (virginica)
    protected final FeatureData virginicaOne = makeIrisFeature(6.3, 3.3, 6., 2.5);
    // the 105th instance of the iris dataset, class 2 (virginica)
    protected final FeatureData virginicaFive = makeIrisFeature(6.5, 3., 5.8, 2.2);

    private static FeatureData makeIrisFeature(double sepalLength, double sepalWidth, double petalLength, double petalWidth) {
        return FeatureData.builder().addFeatures(Map.of(
                "sepal_length", sepalLength,
                "sepal_width", sepalWidth,
                "petal_length", petalLength,
                "petal_width", petalWidth)).build();
    }
}
