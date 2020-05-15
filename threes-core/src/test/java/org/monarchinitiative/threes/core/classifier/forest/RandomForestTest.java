package org.monarchinitiative.threes.core.classifier.forest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.TestBasedOnIrisInstances;
import org.monarchinitiative.threes.core.classifier.TestTreeInstances;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RandomForestTest extends TestBasedOnIrisInstances {

    private RandomForest<FeatureData> forest;

    @BeforeEach
    void setUp() {
        // this test consists of two trees only
        forest = RandomForest.builder()
                .classes(List.of(0, 1, 2))
                .addTrees(List.of(TestTreeInstances.getRandomForestTreeOne(),
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
        assertThat(forest.predictProba(setosaOne).toArray(), is(new double[]{1., 0., 0.}));
        assertThat(forest.predictProba(setosaFive).toArray(), is(new double[]{1., 0., 0.}));
        assertThat(forest.predictProba(versicolorOne).toArray(), is(new double[]{0., .9480851063829787, .05191489361702127}));
        assertThat(forest.predictProba(versicolorFive).toArray(), is(new double[]{0., .9480851063829787, .05191489361702127}));
        assertThat(forest.predictProba(virginicaOne).toArray(), is(new double[]{0., .011111111111111112, .9888888888888889}));
        assertThat(forest.predictProba(virginicaFive).toArray(), is(new double[]{0., .011111111111111112, .9888888888888889}));
    }
}