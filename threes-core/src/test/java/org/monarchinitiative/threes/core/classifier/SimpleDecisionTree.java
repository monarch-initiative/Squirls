package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.classifier.tree.AbstractDecisionTree;

import java.util.Map;
import java.util.function.Function;

/**
 * The simplest implementation of the decision tree for using with the Iris dataset.
 */
public class SimpleDecisionTree extends AbstractDecisionTree<FeatureData> {

    protected SimpleDecisionTree(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Map<Integer, Function<FeatureData, Double>> getFeatureMap() {
        return Map.of(
                0, fd -> fd.getFeature(0, Double.class).orElseThrow(RuntimeException::new),
                1, fd -> fd.getFeature(1, Double.class).orElseThrow(RuntimeException::new),
                2, fd -> fd.getFeature(2, Double.class).orElseThrow(RuntimeException::new),
                3, fd -> fd.getFeature(3, Double.class).orElseThrow(RuntimeException::new));
    }

    public static class Builder extends AbstractDecisionTree.Builder<Builder> {

        @Override
        public SimpleDecisionTree build() {
            return new SimpleDecisionTree(this);
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}
