package org.monarchinitiative.threes.core;

import org.monarchinitiative.threes.core.classifier.Prediction;

import java.util.Collections;
import java.util.Map;

/**
 * Classes that implement this interface represent predictions and accompanying data gathered during analysis of
 * a single variant.
 */
public interface SplicingPredictionData {

    SplicingPredictionData EMPTY = new SplicingPredictionData() {
        @Override
        public Map<String, Prediction> getPredictions() {
            return Collections.emptyMap();
        }

        @Override
        public Metadata getMetadata() {
            return Metadata.empty();
        }
    };

    Map<String, Prediction> getPredictions();

    Metadata getMetadata();

    default boolean hasMetadata() {
        return getMetadata().isEmpty();
    }

    default boolean isEmpty() {
        return this.equals(EMPTY) || (!hasMetadata() && getPredictions().isEmpty());
    }

}
