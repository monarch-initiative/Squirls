package org.monarchinitiative.squirls.core.classifier;

import org.monarchinitiative.squirls.core.SimpleClassifiable;

import java.util.Map;

/**
 * This class defines test instances from Iris dataset.
 */
public class TestBasedOnIrisInstances {

    protected static final double EPSILON = 5e-12;

    // the first instance of the iris dataset, class 0 (setosa)
//    protected final FeatureData setosaOne = makeIrisFeature(5.1, 3.5, 1.4, 0.2);
//     the fifth instance of the iris dataset, class 0 (setosa)
//    protected final FeatureData setosaFive = makeIrisFeature(5., 3.6, 1.4, .2);
    // the fifty-first instance of the iris dataset, class 1 (versicolor)
    protected final Classifiable versicolorOne = makeIrisFeature(7., 3.2, 4.7, 1.4);
    // the fifty-fifth instance of the iris dataset, class 1 (versicolor)
    protected final Classifiable versicolorFive = makeIrisFeature(6.5, 2.8, 4.6, 1.5);
    // the 101st instance of the iris dataset, class 2 (virginica)
    protected final Classifiable virginicaOne = makeIrisFeature(6.3, 3.3, 6., 2.5);
    // the 105th instance of the iris dataset, class 2 (virginica)
    protected final Classifiable virginicaFive = makeIrisFeature(6.5, 3., 5.8, 2.2);

    private static Classifiable makeIrisFeature(double sepalLength, double sepalWidth, double petalLength, double petalWidth) {
        return new SimpleClassifiable(Map.of(
                "sepal_length", sepalLength,
                "sepal_width", sepalWidth,
                "petal_length", petalLength,
                "petal_width", petalWidth));
    }
}
