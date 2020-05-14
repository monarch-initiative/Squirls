package org.monarchinitiative.threes.core.classifier.tree;

import org.monarchinitiative.threes.core.classifier.FeatureData;

import java.util.Map;

public class AcceptorSplicingDecisionTree extends AbstractDecisionTree<FeatureData> {

    private AcceptorSplicingDecisionTree(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Map<Integer, String> getFeatureIndices() {
        /*
         * This is the order of features for acceptor classifier:
         * ['acceptor_offset', 'canonical_acceptor', 'cryptic_acceptor', 'phylop', 'hexamer', 'septamer']
         */
        return Map.of(
                0, "acceptor_offset",
                1, "canonical_acceptor",
                2, "cryptic_acceptor",
                3, "phylop",
                4, "hexamer",
                5, "septamer");
    }

    public static class Builder extends AbstractDecisionTree.Builder<Builder> {

        private Builder() {
            // private no-op
        }

        @Override
        public AcceptorSplicingDecisionTree build() {
            return new AcceptorSplicingDecisionTree(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
