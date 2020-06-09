package org.monarchinitiative.squirls.core.classifier.tree;

import org.monarchinitiative.squirls.core.classifier.Classifiable;

import java.util.Map;
import java.util.Set;

public class AcceptorSplicingDecisionTree<T extends Classifiable> extends AbstractBinaryDecisionTree<T> {

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

    private AcceptorSplicingDecisionTree(Builder<T> builder) {
        super(builder);
    }

    public static <T extends Classifiable> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    protected Map<Integer, String> getFeatureIndices() {
        return featureNames;
    }

    @Override
    public Set<String> usedFeatureNames() {
        return Set.copyOf(featureNames.values());
    }

    public static class Builder<T extends Classifiable> extends AbstractBinaryDecisionTree.Builder<Builder<T>> {

        private Builder() {
            // private no-op
        }

        @Override
        public AcceptorSplicingDecisionTree<T> build() {
            return new AcceptorSplicingDecisionTree<>(this);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }
    }
}
