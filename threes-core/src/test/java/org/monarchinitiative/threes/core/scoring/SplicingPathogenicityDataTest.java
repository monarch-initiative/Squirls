package org.monarchinitiative.threes.core.scoring;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

class SplicingPathogenicityDataTest {

    @Test
    void getMaxValue() {
        final SplicingPathogenicityData instance = SplicingPathogenicityData.builder()
                .putScore("CRYPTIC_ACCEPTOR", 10.0)
                .putScore("CRYPTIC_DONOR", Double.NaN)
                .putScore("CANONICAL_ACCEPTOR", 20.0)
                .putScore("CANONICAL_DONOR", -20.0)
                .build();

        final double maxScore = instance.getMaxScore();
        assertThat(maxScore, is(closeTo(20.0, 0.05)));
    }

    @Test
    void getMaxValueFromEmptyData() {
        final SplicingPathogenicityData empty = SplicingPathogenicityData.empty();
        final double maxEmpty = empty.getMaxScore();
        assertThat(maxEmpty, is(Double.NaN));
    }

    @Test
    void getOrDefault() {
        final SplicingPathogenicityData instance = SplicingPathogenicityData.builder()
                .putScore("CANONICAL_DONOR", 20.0)
                .build();
        final double donor = instance.getOrDefault("CANONICAL_DONOR", 10.0);
        assertThat(donor, is(closeTo(20.0, 0.05)));

        final double acc = instance.getOrDefault("CANONICAL_ACCEPTOR", 15.0);
        assertThat(acc, is(closeTo(15.0, 0.05)));
    }
}