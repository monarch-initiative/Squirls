package org.monarchinitiative.threes.core.classifier.io;

import java.util.List;
import java.util.Objects;

/**
 * This class represents a single
 */
public class DecisionTreeTransferModel {

    private int nodeCount;

    private List<Integer> childrenLeft, childrenRight, feature;

    private List<Double> threshold;
    private List<List<Integer>> values;

    public DecisionTreeTransferModel() {
        // required public no-op
    }

    private DecisionTreeTransferModel(Builder builder) {
        setNodeCount(builder.nodeCount);
        setChildrenLeft(builder.childrenLeft);
        setChildrenRight(builder.childrenRight);
        setFeature(builder.feature);
        setThreshold(builder.threshold);
        setValues(builder.values);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public List<Integer> getChildrenLeft() {
        return childrenLeft;
    }

    public void setChildrenLeft(List<Integer> childrenLeft) {
        this.childrenLeft = childrenLeft;
    }

    public List<Integer> getChildrenRight() {
        return childrenRight;
    }

    public void setChildrenRight(List<Integer> childrenRight) {
        this.childrenRight = childrenRight;
    }

    public List<Integer> getFeature() {
        return feature;
    }

    public void setFeature(List<Integer> feature) {
        this.feature = feature;
    }

    public List<Double> getThreshold() {
        return threshold;
    }

    public void setThreshold(List<Double> threshold) {
        this.threshold = threshold;
    }

    public List<List<Integer>> getValues() {
        return values;
    }

    public void setValues(List<List<Integer>> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionTreeTransferModel decisionTreeTransferModel = (DecisionTreeTransferModel) o;
        return nodeCount == decisionTreeTransferModel.nodeCount &&
                Objects.equals(childrenLeft, decisionTreeTransferModel.childrenLeft) &&
                Objects.equals(childrenRight, decisionTreeTransferModel.childrenRight) &&
                Objects.equals(feature, decisionTreeTransferModel.feature) &&
                Objects.equals(threshold, decisionTreeTransferModel.threshold) &&
                Objects.equals(values, decisionTreeTransferModel.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeCount, childrenLeft, childrenRight, feature, threshold, values);
    }

    @Override
    public String toString() {
        return "ModelData{" +
                "nodeCount=" + nodeCount +
                ", childrenLeft=" + childrenLeft +
                ", childrenRight=" + childrenRight +
                ", feature=" + feature +
                ", threshold=" + threshold +
                ", values=" + values +
                '}';
    }

    public static final class Builder {
        private int nodeCount;
        private List<Integer> childrenLeft;
        private List<Integer> childrenRight;
        private List<Integer> feature;
        private List<Double> threshold;
        private List<List<Integer>> values;

        private Builder() {
        }

        public Builder nodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        public Builder childrenLeft(List<Integer> childrenLeft) {
            this.childrenLeft = childrenLeft;
            return this;
        }

        public Builder childrenRight(List<Integer> childrenRight) {
            this.childrenRight = childrenRight;
            return this;
        }

        public Builder feature(List<Integer> feature) {
            this.feature = feature;
            return this;
        }

        public Builder threshold(List<Double> threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder values(List<List<Integer>> values) {
            this.values = values;
            return this;
        }

        public DecisionTreeTransferModel build() {
            return new DecisionTreeTransferModel(this);
        }
    }
}
