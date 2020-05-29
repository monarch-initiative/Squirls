package org.monarchinitiative.threes.core.classifier;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class StandardPrediction implements Prediction {

    /**
     * List of pairs of prediction & thresholds.
     */
    protected final Set<Fragment> fragments;

    private StandardPrediction(Builder builder) {
        this.fragments = Set.copyOf(builder.scores);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Set<Fragment> getFragments() {
        return fragments;
    }

    @Override
    public boolean isPathogenic() {
        return fragments.stream()
                .anyMatch(Fragment::isPathogenic);
    }

    @Override
    public double getPathoProba() {
        return fragments.stream()
                .mapToDouble(Fragment::getPathoProba)
                .max()
                .orElse(Double.NaN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardPrediction that = (StandardPrediction) o;
        return Objects.equals(fragments, that.fragments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fragments);
    }


    @Override
    public String toString() {
        return "Prediction{" +
                "fragments=" + fragments +
                '}';
    }

    /**
     * This class represents a fragment of information from the decision function of the underlying model which
     * calculated pathogenicity probability.
     */
    public static class Fragment {
        private final double pathoProba;

        private final double threshold;

        private Fragment(double pathoProba, double threshold) {
            this.pathoProba = pathoProba;
            this.threshold = threshold;
        }

        public static Fragment of(double pathoProba, double threshold) {
            return new Fragment(pathoProba, threshold);
        }

        public double getThreshold() {
            return threshold;
        }

        public double getPathoProba() {
            return pathoProba;
        }

        public boolean isPathogenic() {
            return pathoProba > threshold;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Fragment fragment = (Fragment) o;
            return Double.compare(fragment.pathoProba, pathoProba) == 0 &&
                    Double.compare(fragment.threshold, threshold) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pathoProba, threshold);
        }

        @Override
        public String toString() {
            return "Fragment{" +
                    "pathoProba=" + pathoProba +
                    ", threshold=" + threshold +
                    '}';
        }
    }


    public static class Builder {
        protected final Set<Fragment> scores = new HashSet<>();

        private Builder() {
            // private no-op
        }

        public Builder addProbaThresholdPair(double proba, double threshold) {
            scores.add(Fragment.of(proba, threshold));
            return this;
        }

        public StandardPrediction build() {
            return new StandardPrediction(this);
        }
    }
}
