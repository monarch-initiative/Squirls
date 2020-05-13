package org.monarchinitiative.threes.core.classifier.forest;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.ClassifierTestBasedOnIrisInstances;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.TestTreeInstances;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RandomForestTest extends ClassifierTestBasedOnIrisInstances {

    private RandomForest<FeatureData> forest;

    @BeforeEach
    void setUp() {
        // this test consists of two trees only
        forest = RandomForest.<FeatureData>builder()
                .classes(List.of(0, 1, 2))
                .trees(List.of(TestTreeInstances.getRandomForestTreeOne(),
                        TestTreeInstances.getRandomForestTreeTwo()))
                .build();
    }

    @Test
    void predict() {
        assertThat(forest.predict(setosaOne), is(0));
        assertThat(forest.predict(setosaFive), is(0));
        assertThat(forest.predict(versicolorOne), is(1));
        assertThat(forest.predict(versicolorFive), is(1));
        assertThat(forest.predict(virginicaOne), is(2));
        assertThat(forest.predict(virginicaFive), is(2));
    }

    @Test
    void predictProba() {
        assertThat(forest.predictProba(setosaOne), is(new DoubleMatrix(new double[]{1., 0., 0.})));
        assertThat(forest.predictProba(setosaFive), is(new DoubleMatrix(new double[]{1., 0., 0.})));
        assertThat(forest.predictProba(versicolorOne), is(new DoubleMatrix(new double[]{0., .94808511, .05191489})));
        assertThat(forest.predictProba(versicolorFive), is(new DoubleMatrix(new double[]{0., .94808511, .05191489})));
        assertThat(forest.predictProba(virginicaOne), is(new DoubleMatrix(new double[]{0., .01111111, .98888889})));
        assertThat(forest.predictProba(virginicaFive), is(new DoubleMatrix(new double[]{0., .01111111, .98888889})));
    }
}