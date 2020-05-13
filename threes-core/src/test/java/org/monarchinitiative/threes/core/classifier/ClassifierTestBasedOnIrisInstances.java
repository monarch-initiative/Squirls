package org.monarchinitiative.threes.core.classifier;

import java.util.Map;

/**
 * This class defines test instances from
 */
public class ClassifierTestBasedOnIrisInstances {

    // the first instance of the iris dataset, class 0 (setosa)
    protected final FeatureData setosaOne = SimpleFeatureData.of(
            Map.of(0, 5.1,
                    1, 3.5,
                    2, 1.4,
                    3, 0.2));

    // the fifth instance of the iris dataset, class 0 (setosa)
    protected final FeatureData setosaFive = SimpleFeatureData.of(
            Map.of(0, 5.,
                    1, 3.6,
                    2, 1.4,
                    3, 0.2));

    // the fifty-first instance of the iris dataset, class 1 (versicolor)
    protected final FeatureData versicolorOne = SimpleFeatureData.of(
            Map.of(0, 7.,
                    1, 3.2,
                    2, 4.7,
                    3, 1.4));

    // the fifty-fifth instance of the iris dataset, class 1 (versicolor)
    protected final FeatureData versicolorFive = SimpleFeatureData.of(
            Map.of(0, 6.5,
                    1, 2.8,
                    2, 4.6,
                    3, 1.5));
    // the 101st instance of the iris dataset, class 2 (virginica)
    protected final FeatureData virginicaOne = SimpleFeatureData.of(
            Map.of(0, 6.3,
                    1, 3.3,
                    2, 6.,
                    3, 2.5));

    // the 105th instance of the iris dataset, class 2 (virginica)
    protected final FeatureData virginicaFive = SimpleFeatureData.of(
            Map.of(0, 6.5,
                    1, 3,
                    2, 5.8,
                    3, 2.2));
}
