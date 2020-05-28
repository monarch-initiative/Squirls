package org.monarchinitiative.threes.core.classifier.tree;

import org.monarchinitiative.threes.core.classifier.FeatureData;

import java.util.Map;
import java.util.Set;

public class AcceptorSplicingDecisionTree extends AbstractBinaryDecisionTree<FeatureData> {

    /*
     * This is the order of features for acceptor classifier:
     * ['acceptor_offset', 'canonical_acceptor', 'cryptic_acceptor', 'phylop', 'hexamer', 'septamer']
     */
    private static final Map<Integer, String> featureNames = Map.of(
            0, "acceptor_offset",
            1, "canonical_acceptor",
            2, "cryptic_acceptor",
            3, "phylop",
            4, "hexamer",
            5, "septamer");

    private AcceptorSplicingDecisionTree(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Map<Integer, String> getFeatureIndices() {
        return featureNames;
    }

    @Override
    public Set<String> usedFeatureNames() {
        return Set.copyOf(featureNames.values());
    }

    public static class Builder extends AbstractBinaryDecisionTree.Builder<Builder> {

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
