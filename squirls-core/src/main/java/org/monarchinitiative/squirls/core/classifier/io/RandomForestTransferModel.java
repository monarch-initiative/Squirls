package org.monarchinitiative.squirls.core.classifier.io;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RandomForestTransferModel {

    private List<Integer> classes;

    private Map<Integer, DecisionTreeTransferModel> trees;

    public RandomForestTransferModel() {
        // required public no-op
    }

    private RandomForestTransferModel(Builder builder) {
        setClasses(builder.classes);
        setTrees(builder.trees);
    }

    public static Builder builder() {
        return new Builder();
    }


    public List<Integer> getClasses() {
        return classes;
    }

    public void setClasses(List<Integer> classes) {
        this.classes = classes;
    }

    public Map<Integer, DecisionTreeTransferModel> getTrees() {
        return trees;
    }

    public void setTrees(Map<Integer, DecisionTreeTransferModel> trees) {
        this.trees = trees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomForestTransferModel that = (RandomForestTransferModel) o;
        return Objects.equals(classes, that.classes) &&
                Objects.equals(trees, that.trees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classes, trees);
    }

    @Override
    public String toString() {
        return "RandomForestTransferModel{" +
                "classes=" + classes +
                ", trees=" + trees +
                '}';
    }

    public static final class Builder {
        private List<Integer> classes;
        private Map<Integer, DecisionTreeTransferModel> trees;

        private Builder() {
        }

        public Builder classes(List<Integer> classes) {
            this.classes = classes;
            return this;
        }

        public Builder trees(Map<Integer, DecisionTreeTransferModel> trees) {
            this.trees = trees;
            return this;
        }

        public RandomForestTransferModel build() {
            return new RandomForestTransferModel(this);
        }
    }
}
