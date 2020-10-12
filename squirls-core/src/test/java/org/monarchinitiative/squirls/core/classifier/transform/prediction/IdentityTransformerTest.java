package org.monarchinitiative.squirls.core.classifier.transform.prediction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;

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
                .addProbaThresholdPair("anything", .500000000, .123456)
                .build();

        SimpleMutablePrediction prd = new SimpleMutablePrediction();
        prd.setPrediction(prediction);

        SimpleMutablePrediction transPrd = transformer.transform(prd);

        assertThat(transPrd.getPrediction().getPartialPredictions(), hasSize(1));
        assertThat(transPrd.getPrediction().getPartialPredictions(), hasItems(Prediction.PartialPrediction.of("anything", .500000000, .123456)));
    }

}