package org.monarchinitiative.threes.core.scoring;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * POJO for results of splicing analysis.
 */
public class SplicingPathogenicityData {

    private static final SplicingPathogenicityData EMPTY = SplicingPathogenicityData.builder().build();

    private final ImmutableMap<String, Double> scoresMap;

    private SplicingPathogenicityData(Builder builder) {
        scoresMap = ImmutableMap.copyOf(builder.scoresMap);
    }

    public static SplicingPathogenicityData empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ImmutableMap<String, Double> getScoresMap() {
        return scoresMap;
    }

    public double getOrDefault(String strategy, double defaultValue) {
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
        if (o == null || getClass() != o.getClass()) return false;
        SplicingPathogenicityData that = (SplicingPathogenicityData) o;
        return Objects.equals(scoresMap, that.scoresMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scoresMap);
    }

    @Override
    public String toString() {
        return "SplicingPathogenicityData{" +
                "scoresMap=" + scoresMap +
                '}';
    }

    public static final class Builder {

        private Map<String, Double> scoresMap;

        private Builder() {
            this.scoresMap = new HashMap<>();
        }

        public Builder putAllScores(Map<String, Double> scoresMap) {
            this.scoresMap.putAll(scoresMap);
            return this;
        }

        public Builder putScore(String strategy, double score) {
            this.scoresMap.put(strategy, score);
            return this;
        }

        public SplicingPathogenicityData build() {
            return new SplicingPathogenicityData(this);
        }
    }
}
