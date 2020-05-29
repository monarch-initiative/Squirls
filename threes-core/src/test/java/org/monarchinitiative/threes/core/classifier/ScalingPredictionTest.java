package org.monarchinitiative.threes.core.classifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScalingPredictionTest {

    private static final double EPSILON = 5E-6;

    @Test
    void predict() {
        ScalingPrediction prediction = ScalingPrediction.builder()
                .setDonorData(.101, .100)
                .slope(Double.NaN) // does not matter here
                .intercept(Double.NaN) // does not matter here
                .build();

        assertTrue(prediction.isPathogenic());

        prediction = ScalingPrediction.builder()
                .setDonorData(.100, .100)
                .slope(Double.NaN) // does not matter here
                .intercept(Double.NaN) // does not matter here
                .build();

        assertFalse(prediction.isPathogenic());
    }

    /**
     * We test that scaling works for donor.
     */
    @ParameterizedTest
    @CsvSource({"0.004012,0.007730", "0.525658,0.905913", "0.004538,0.007785"})
    void getPathoProbaDonor(double proba, double expected) {
        double donorThreshold = Double.NaN;

        // these parameters stay fixed
        final double slope = 13.64842177;
        final double intercept = -4.90967636;

        final ScalingPrediction prediction = ScalingPrediction.builder()
                .setDonorData(proba, donorThreshold)
                .slope(slope)
                .intercept(intercept)
                .build();

        final double actual = prediction.getPathoProba();
        assertThat(actual, is(closeTo(expected, EPSILON)));
    }

    /**
     * We test that scaling works also for acceptor.
     */
    @ParameterizedTest
    @CsvSource({"0.004012,0.007730", "0.525658,0.905913", "0.004538,0.007785"})
    void getPathoProbaAcceptor(double proba, double expected) {
        double threshold = Double.NaN; // must not matter here

        // these parameters stay fixed
        final double slope = 13.64842177;
        final double intercept = -4.90967636;

        final ScalingPrediction prediction = ScalingPrediction.builder()
                .setAcceptorData(proba, threshold)
                .slope(slope)
                .intercept(intercept)
                .build();

        final double actual = prediction.getPathoProba();
        assertThat(actual, is(closeTo(expected, EPSILON)));
    }
}