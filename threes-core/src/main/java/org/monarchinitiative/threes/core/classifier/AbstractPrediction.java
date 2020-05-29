package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.Prediction;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPrediction implements Prediction {

    protected final Map<Culprit, Fragment> data;

    protected AbstractPrediction(Builder<?> builder) {
        this.data = Map.copyOf(builder.data);
    }

    @Override
    public boolean isPathogenic() {
        return data.values().stream()
                .anyMatch(Fragment::isPathogenic);
    }

    @Override
    public abstract double getPathoProba();


    protected enum Culprit {
        DONOR,
        ACCEPTOR
    }

    /**
     * This class represents a fragment of information from the decision function of the underlying model which
     * calculated pathogenicity probability.
     */
    protected static class Fragment {
        private final double pathoProba;

        private final double threshold;

        public Fragment(double pathoProba, double threshold) {
            this.pathoProba = pathoProba;
            this.threshold = threshold;
        }

        public double getThreshold() {
            return threshold;
        }

        public double getPathoProba() {
            return pathoProba;
        }

        boolean isPathogenic() {
            return pathoProba > threshold;
        }
    }

    protected abstract static class Builder<T extends Builder<T>> {
        private final Map<Culprit, Fragment> data = new HashMap<>();

        public T setDonorData(double proba, double threshold) {
            this.data.put(Culprit.DONOR, new Fragment(proba, threshold));
            return self();
        }

        public T setAcceptorData(double proba, double threshold) {
            this.data.put(Culprit.ACCEPTOR, new Fragment(proba, threshold));
            return self();
        }

        protected abstract Prediction build();

        protected abstract T self();
    }

}
