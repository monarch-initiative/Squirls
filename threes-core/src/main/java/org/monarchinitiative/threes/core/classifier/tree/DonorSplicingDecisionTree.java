package org.monarchinitiative.threes.core.classifier.tree;

import org.monarchinitiative.threes.core.classifier.FeatureData;

import java.util.Map;

public class DonorSplicingDecisionTree extends AbstractDecisionTree<FeatureData> {

    private DonorSplicingDecisionTree(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Map<Integer, String> getFeatureIndices() {
        /*
         * This is the order of features for donor classifier:
         * ['donor_offset', 'canonical_donor', 'cryptic_donor', 'phylop', 'hexamer', 'septamer']
         */
        return Map.of(
                0, "donor_offset",
                1, "canonical_donor",
                2, "cryptic_donor",
                3, "phylop",
                4, "hexamer",
                5, "septamer");
    }

    public static class Builder extends AbstractDecisionTree.Builder<Builder> {

        private Builder() {
            // private no-op
        }

        @Override
        public DonorSplicingDecisionTree build() {
            return new DonorSplicingDecisionTree(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
