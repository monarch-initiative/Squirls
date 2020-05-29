package org.monarchinitiative.threes.core.classifier.prediction_transform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.Prediction;
import org.monarchinitiative.threes.core.classifier.StandardPrediction;
import org.monarchinitiative.threes.core.classifier.transform.prediction.IdentityTransformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

class IdentityTransformerTest {

    private IdentityTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = IdentityTransformer.getInstance();
    }

    @Test
    void transform() {
        final Prediction prediction = StandardPrediction.builder()
                .addProbaThresholdPair(.500000000, .123456)
                .build();

        final Prediction transformed = transformer.transform(prediction);

        assertThat(transformed.getPartialPredictions(), hasSize(1));
        assertThat(transformed.getPartialPredictions(), hasItems(Prediction.PartialPrediction.of(.500000000, .123456)));
    }
}