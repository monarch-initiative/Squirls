package org.monarchinitiative.threes.core.classifier.tree;

import org.monarchinitiative.threes.core.classifier.Classifiable;

import java.util.Map;
import java.util.Set;

public class DonorSplicingDecisionTree<T extends Classifiable> extends AbstractBinaryDecisionTree<T> {

    /**
     * This is the order of features for donor classifier:
     * ['donor_offset', 'canonical_donor', 'cryptic_donor', 'phylop', 'hexamer', 'septamer']
     */
    private static final Map<Integer, String> featureNames = Map.of(
            0, "donor_offset",
            1, "canonical_donor",
            2, "cryptic_donor",
            3, "phylop",
            4, "hexamer",
            5, "septamer");

    private DonorSplicingDecisionTree(Builder<T> builder) {
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
        public DonorSplicingDecisionTree<T> build() {
            return new DonorSplicingDecisionTree<>(this);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }
    }

}
