package org.monarchinitiative.squirls.core.classifier;

import org.monarchinitiative.squirls.core.classifier.tree.AbstractBinaryDecisionTree;

import java.util.Map;
import java.util.Set;

/**
 * The simplest implementation of the decision tree for using with the Iris dataset.
 */
public class IrisDecisionTree extends AbstractBinaryDecisionTree<Classifiable> {

    private static final Map<Integer, String> featureIndices = Map.of(0, "sepal_length",
            1, "sepal_width",
            2, "petal_length",
            3, "petal_width");

    protected IrisDecisionTree(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Map<Integer, String> getFeatureIndices() {
        return featureIndices;
    }

    @Override
    public Set<String> usedFeatureNames() {
        return Set.copyOf(featureIndices.values());
    }

    public static class Builder extends AbstractBinaryDecisionTree.Builder<Builder> {

        @Override
        public IrisDecisionTree build() {
            return new IrisDecisionTree(this);
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}