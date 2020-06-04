package org.monarchinitiative.threes.core;

import org.monarchinitiative.threes.core.classifier.Prediction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StandardSplicingPredictionData implements SplicingPredictionData {

    private final Map<String, Prediction> predictionMap;

    private final Metadata metadata;

    private StandardSplicingPredictionData(Builder builder) {
        predictionMap = Map.copyOf(builder.predictionMap);
        metadata = Objects.requireNonNull(builder.metadata);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Map<String, Prediction> getPredictions() {
        return predictionMap;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    public static final class Builder {
        private final Map<String, Prediction> predictionMap = new HashMap<>();
        private Metadata metadata;

        private Builder() {
        }

        public Builder predictionMap(Map<String, Prediction> predictionMap) {
            this.predictionMap.putAll(predictionMap);
            return this;
        }

        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public StandardSplicingPredictionData build() {
            return new StandardSplicingPredictionData(this);
        }
    }
}
