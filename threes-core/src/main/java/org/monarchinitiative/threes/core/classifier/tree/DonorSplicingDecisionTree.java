package org.monarchinitiative.threes.core.classifier.tree;

import org.monarchinitiative.threes.core.classifier.FeatureData;

import java.util.Map;
import java.util.Set;

public class DonorSplicingDecisionTree extends AbstractBinaryDecisionTree<FeatureData> {

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

    private DonorSplicingDecisionTree(Builder builder) {
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
        public DonorSplicingDecisionTree build() {
            return new DonorSplicingDecisionTree(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
