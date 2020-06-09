package org.monarchinitiative.threes.core.classifier.forest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.Classifiable;
import org.monarchinitiative.threes.core.classifier.TestBasedOnIrisInstances;
import org.monarchinitiative.threes.core.classifier.TestTreeInstances;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class RandomForestTest extends TestBasedOnIrisInstances {

    private RandomForest<Classifiable> forest;

    @BeforeEach
    void setUp() {
        // this test consists of two trees only
        forest = RandomForest.builder()
                .classes(List.of(1, 2))
                .addTrees(List.of(TestTreeInstances.getRandomForestTreeOne(),
                        TestTreeInstances.getRandomForestTreeTwo()))
                .build();
    }

    @Test
    void predictProba() {
        assertThat(forest.predictProba(versicolorOne), is(closeTo(0., EPSILON)));
        assertThat(forest.predictProba(versicolorFive), is(closeTo(0., EPSILON)));
        assertThat(forest.predictProba(virginicaOne), is(closeTo(1., EPSILON)));
        assertThat(forest.predictProba(virginicaFive), is(closeTo(1., EPSILON)));
    }
}