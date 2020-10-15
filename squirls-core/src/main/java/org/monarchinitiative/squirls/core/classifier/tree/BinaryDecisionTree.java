package org.monarchinitiative.squirls.core.classifier.tree;

import org.monarchinitiative.squirls.core.classifier.AbstractBinaryClassifier;
import org.monarchinitiative.squirls.core.classifier.Classifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An implementation of a decision tree that only supports prediction, not training.
 *
 * @param <T>
 */
public class BinaryDecisionTree<T extends Classifiable> extends AbstractBinaryClassifier<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDecisionTree.class);

    /**
     * In arrays that define this tree, this is the index of the root node.
     */
    private static final int ROOT_IDX = 0;

    /**
     * How many nodes this decision tree consists of.
     */
    private final int nNodes;

    /**
     * Expecting to see a vector with {@link #nNodes} elements containing index of feature that is supposed to be used \
     * for making a decision in a particular decision node.
     */
    private final int[] features;

    /**
     * Expecting to see a vector with {@link #nNodes} elements containing threshold that is used when making a decision
     * in a particular decision node.
     */
    private final double[] thresholds;

    /**
     * Expecting to see a vector with {@link #nNodes} elements containing index of left child node for a particular
     * node. The index corresponds to <code>-1</code> if the node does <em>not</em> have a left child.
     */
    private final int[] childrenLeft;

    /**
     * Expecting to see a vector with {@link #nNodes} elements containing index of right child node for a particular
     * node. The index corresponds to <code>-1</code> if the node does <em>not</em> have a right child.
     */
    private final int[] childrenRight;

    /**
     * Expecting to see an array/matrix with shape (nNodes, nClasses)
     */
    private final int[][] classCounts;

    /**
     * When the model was trained in python, the feature matrix had columns in certain order. This map
     * translates feature names into the feature matrix order to ensure that feature name corresponds to the appropriate
     * entry in {@link #features} array.
     */
    private final Map<Integer, String> featureIndices;

    private BinaryDecisionTree(Builder<T> builder) {
        super(builder);
        this.nNodes = builder.nNodes;
        this.features = toIntArray(builder.features);
        this.featureIndices = Map.copyOf(builder.featureIndices);
        this.thresholds = toDoubleArray(builder.thresholds);
        this.childrenLeft = toIntArray(builder.childrenLeft);
        this.childrenRight = toIntArray(builder.childrenRight);
        this.classCounts = toNestedIntArray(builder.values);
        check();
    }

    /**
     * Convert list of lists of integers to a nested integer array.
     *
     * @param values list of lists of integers
     * @return nested integer array
     */
    private static int[][] toNestedIntArray(List<List<Integer>> values) {
        final int[][] array = new int[values.size()][];
        for (int i = 0; i < values.size(); i++) {
            final List<Integer> innerList = values.get(i);
            final int[] inner = new int[innerList.size()];
            for (int j = 0; j < innerList.size(); j++) {
                inner[j] = innerList.get(j);
            }
            array[i] = inner;
        }
        return array;
    }

    public static <T extends Classifiable> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Sanity checks for the abstract decision tree.
     * <p>
     * We check that:
     * <ul>
     *     <li># feature indices matches # nodes</li>
     *     <li># thresholds matches # nodes</li>
     *     <li># childrenLeft matches # nodes</li>
     *     <li># childrenRight matches # nodes</li>
     *     <li># values matches # nodes</li>
     *     <li>each value array contains the same number of elements as there are labels in `classes` array</li>
     * </ul>
     */
    private void check() {
        if (nNodes != features.length) {
            String msg = String.format("#feature indices (`%d`) must match #nodes (`%d`)", nNodes, features.length);
            LOGGER.warn(msg);
            throw new RuntimeException(msg);
        }
        if (nNodes != thresholds.length) {
            String msg = String.format("#thresholds (`%d`) must match #nodes (`%d`)", nNodes, thresholds.length);
            LOGGER.warn(msg);
            throw new RuntimeException(msg);
        }
        if (nNodes != childrenLeft.length) {
            String msg = String.format("#childrenLeft (`%d`) must match #nodes (`%d`)", nNodes, childrenLeft.length);
            LOGGER.warn(msg);
            throw new RuntimeException(msg);
        }
        if (nNodes != childrenRight.length) {
            String msg = String.format("#childrenRight (`%d`) must match #nodes (`%d`)", nNodes, childrenRight.length);
            LOGGER.warn(msg);
            throw new RuntimeException(msg);
        }
        if (nNodes != classCounts.length) {
            String msg = String.format("#class counts (`%d`) must match #nodes (`%d`)", nNodes, classCounts.length);
            LOGGER.warn(msg);
            throw new RuntimeException(msg);
        }
        if (!Arrays.stream(classCounts)
                .map(classCount -> classCount.length)
                .allMatch(i -> i == classes.length)) {
            String msg = "All `values` arrays must have the same length as the `classes` array";
            LOGGER.warn(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public Set<String> usedFeatureNames() {
        return Set.copyOf(featureIndices.values());
    }

    /**
     * Predict class probabilities for given instance. Expecting to get an instance with all features available.
     *
     * @param instance to be used for prediction
     * @return label of the predicted class
     */
    @Override
    public double predictProba(T instance) {
        return predictProba(instance, ROOT_IDX);
    }

    /**
     * Get class probabilities of this instance.
     *
     * @param instance instance
     * @param nodeIdx  index of the  node that is currently being processed
     * @return class probabilities in the same order as in {@link #classes} attribute
     */
    private double predictProba(T instance, int nodeIdx) {
        if (childrenLeft[nodeIdx] != childrenRight[nodeIdx]) {
            /*
             * Indices of the child nodes are not equal. In context of our model, this means that the node
             * is a decision node and not a leaf. We make a decision by:
             * - get threshold
             * - test feature value and select idx of left/right node
             * - recurse down
             */
            final int featureIdx = features[nodeIdx];
            final String featureName = featureIndices.get(featureIdx);

            /*
             We should not get null pointer here since we check that we have all the features at the level of
             SquirlsClassifier.
             */
            final double feature = instance.getFeature(featureName, Double.class);
            final double threshold = thresholds[nodeIdx];

            return (feature <= threshold)
                    ? predictProba(instance, childrenLeft[nodeIdx])
                    : predictProba(instance, childrenRight[nodeIdx]);
        } else {
            /*
             * Both indices are -1, this is a leaf node.
             * We are making a prediction here
             */
            // how many samples of each class do we have in this particular node?
            final int[] classCounts = this.classCounts[nodeIdx];
            double sum = 0;
            for (int count : classCounts) {
                sum += count;
            }
            /*
             Calculate probability as a fraction of positive samples existing in this node. The number of positive
             samples is stored under idx 1
             */
            int nPathogenicSamples = classCounts[1];

            return nPathogenicSamples / sum;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryDecisionTree<?> that = (BinaryDecisionTree<?>) o;
        return nNodes == that.nNodes &&
                Arrays.equals(features, that.features) &&
                Arrays.equals(thresholds, that.thresholds) &&
                Arrays.equals(childrenLeft, that.childrenLeft) &&
                Arrays.equals(childrenRight, that.childrenRight) &&
                Arrays.equals(classCounts, that.classCounts);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(nNodes);
        result = 31 * result + Arrays.hashCode(features);
        result = 31 * result + Arrays.hashCode(thresholds);
        result = 31 * result + Arrays.hashCode(childrenLeft);
        result = 31 * result + Arrays.hashCode(childrenRight);
        result = 31 * result + Arrays.hashCode(classCounts);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractBinaryDecisionTree{" +
                "nNodes=" + nNodes +
                ", features=" + Arrays.toString(features) +
                ", thresholds=" + Arrays.toString(thresholds) +
                ", childrenLeft=" + Arrays.toString(childrenLeft) +
                ", childrenRight=" + Arrays.toString(childrenRight) +
                ", classCounts=" + Arrays.stream(classCounts).map(Arrays::toString).collect(Collectors.joining(",")) +
                "} " + super.toString();
    }

    public static class Builder<T extends Classifiable> extends AbstractBinaryClassifier.Builder<Builder<T>> {

        private final Map<Integer, String> featureIndices = new HashMap<>();

        private int nNodes;

        private List<Integer> features;

        private List<Double> thresholds;

        private List<Integer> childrenLeft;

        private List<Integer> childrenRight;

        private List<List<Integer>> values;

        protected Builder() {
            // protected no-op
        }

        @Override
        public BinaryDecisionTree<T> build() {
            return new BinaryDecisionTree<>(this);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }

        public Builder<T> nNodes(int nNodes) {
            this.nNodes = nNodes;
            return self();
        }

        public Builder<T> putFeatureIndex(int index, String feature) {
            featureIndices.put(index, feature);
            return self();
        }

        public Builder<T> putAllFeatureIndices(Map<? extends Integer, ? extends String> featureIndices) {
            this.featureIndices.putAll(featureIndices);
            return self();
        }

        public Builder<T> features(List<Integer> features) {
            this.features = List.copyOf(features);
            return self();
        }

        public Builder<T> thresholds(List<Double> thresholds) {
            this.thresholds = List.copyOf(thresholds);
            return self();
        }

        public Builder<T> childrenLeft(List<Integer> childrenLeft) {
            this.childrenLeft = List.copyOf(childrenLeft);
            return self();
        }

        public Builder<T> childrenRight(List<Integer> childrenRight) {
            this.childrenRight = List.copyOf(childrenRight);
            return self();
        }

        public Builder<T> values(List<List<Integer>> values) {
            this.values = List.copyOf(values);
            return self();
        }

    }
}
