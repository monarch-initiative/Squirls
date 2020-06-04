package org.monarchinitiative.threes.core;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.Prediction;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplicingPredictionDataTest {

    @Test
    void isEmpty() {
        assertTrue(SplicingPredictionData.EMPTY.isEmpty());
    }

    @Test
    void isEmptyEvenWithEmptyPredictionsMap() {
        SplicingPredictionData data = new SplicingPredictionData() {
            @Override
            public Map<String, Prediction> getPredictions() {
                return Map.of();
            }

            @Override
            public Metadata getMetadata() {
                return Metadata.empty();
            }
        };
        assertFalse(data.isEmpty());
    }
}