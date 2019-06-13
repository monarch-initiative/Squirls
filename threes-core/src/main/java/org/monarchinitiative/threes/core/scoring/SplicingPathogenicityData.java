package org.monarchinitiative.threes.core.scoring;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SplicingPathogenicityData {

    private static final SplicingPathogenicityData EMPTY = SplicingPathogenicityData.newBuilder().build();

    private final ImmutableMap<ScoringStrategy, Double> scoresMap;

    private SplicingPathogenicityData(Builder builder) {
        scoresMap = ImmutableMap.copyOf(builder.scoresMap);
    }

    public static SplicingPathogenicityData empty() {
        return EMPTY;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public ImmutableMap<ScoringStrategy, Double> getScoresMap() {
        return scoresMap;
    }

    public double getOrDefault(ScoringStrategy strategy, double defaultValue) {
        return scoresMap.getOrDefault(strategy, defaultValue);
    }

    public double getMaxScore() {
        return scoresMap.values().stream()
                .filter(score -> !score.isNaN())
                .max(Double::compareTo)
                .orElse(Double.NaN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplicingPathogenicityData)) return false;

        SplicingPathogenicityData that = (SplicingPathogenicityData) o;

        return scoresMap != null ? scoresMap.equals(that.scoresMap) : that.scoresMap == null;

    }

    @Override
    public int hashCode() {
        return scoresMap != null ? scoresMap.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SplicingPathogenicityData{" +
                "scoresMap=" + scoresMap +
                '}';
    }

    public static final class Builder {

        private Map<ScoringStrategy, Double> scoresMap;

        private Builder() {
            this.scoresMap = new HashMap<>();
        }

        public Builder putAllScores(Map<ScoringStrategy, Double> scoresMap) {
            this.scoresMap.putAll(scoresMap);
            return this;
        }

        public Builder putScore(ScoringStrategy strategy, double score) {
            this.scoresMap.put(strategy, score);
            return this;
        }

        public SplicingPathogenicityData build() {
            return new SplicingPathogenicityData(this);
        }
    }
}
