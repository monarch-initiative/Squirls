package org.monarchinitiative.threes.core.classifier.tree;

import org.monarchinitiative.threes.core.classifier.FeatureData;

import java.util.Map;
import java.util.function.Function;

public class DonorSplicingDecisionTree extends AbstractDecisionTree<FeatureData> {

    private DonorSplicingDecisionTree(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Map<Integer, Function<FeatureData, Double>> getFeatureMap() {
        /*
         * This is the order of features for donor classifier:
         * ['donor_offset', 'canonical_donor', 'cryptic_donor', 'phylop', 'hexamer', 'septamer']
         */
        return Map.of(
                0, sf -> sf.getFeature(0, Double.class).orElseThrow(RuntimeException::new), // donor_offset
                1, sf -> sf.getFeature(1, Double.class).orElseThrow(RuntimeException::new), // canonical_donor
                2, sf -> sf.getFeature(2, Double.class).orElseThrow(RuntimeException::new), // cryptic_donor
                3, sf -> sf.getFeature(3, Double.class).orElseThrow(RuntimeException::new), // phylop
                4, sf -> sf.getFeature(4, Double.class).orElseThrow(RuntimeException::new), // hexamer
                5, sf -> sf.getFeature(5, Double.class).orElseThrow(RuntimeException::new) // septamer
        );
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
