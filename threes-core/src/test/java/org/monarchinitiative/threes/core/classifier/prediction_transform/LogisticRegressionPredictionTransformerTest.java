package org.monarchinitiative.threes.core.classifier.prediction_transform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.threes.core.classifier.Prediction;
import org.monarchinitiative.threes.core.classifier.StandardPrediction;
import org.monarchinitiative.threes.core.classifier.transform.prediction.LogisticRegressionPredictionTransformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class LogisticRegressionPredictionTransformerTest {

    private static final double EPSILON = 5E-6;

    private static final double SLOPE = 13.64842177;
    private static final double INTERCEPT = -4.90967636;

    private LogisticRegressionPredictionTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = LogisticRegressionPredictionTransformer.getInstance(SLOPE, INTERCEPT);
    }

    @ParameterizedTest
    @CsvSource({"0.004012,0.007730", "0.525658,0.905913", "0.004538,0.007785"})
    void transform(double proba, double expectedProba) {
        double threshold = .5;
        double expectedThreshold = 0.871527;

        Prediction prediction = StandardPrediction.builder()
                .addProbaThresholdPair(proba, threshold)
                .build();

        final Prediction transformed = transformer.transform(prediction);
        assertThat(transformed.getPartialPredictions(), hasSize(1));

        @SuppressWarnings("OptionalGetWithoutIsPresent") final Prediction.PartialPrediction partial = transformed.getPartialPredictions().stream().findFirst().get();
        assertThat(partial.getPathoProba(), is(closeTo(expectedProba, EPSILON)));
        assertThat(partial.getThreshold(), is(closeTo(expectedThreshold, EPSILON)));
    }
}