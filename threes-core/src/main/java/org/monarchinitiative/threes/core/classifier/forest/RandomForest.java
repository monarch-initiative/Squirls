package org.monarchinitiative.threes.core.classifier.forest;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.classifier.AbstractClassifier;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.tree.AbstractDecisionTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class uses a collection of {@link AbstractDecisionTree}s to perform classification of a {@link T} instance.
 * <p>
 * When making class predictions the prediction is based on the most likely class label as identified by
 * {@link #predictProba(T)}.
 * <p>
 * When predicting probabilities, a mean probability values of individual trees is provided.
 *
 * <p>
 * The predictions are being performed in parallel.
 *
 * @param <T> type of the data point
 */
public class RandomForest<T extends FeatureData> extends AbstractClassifier<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomForest.class);

    private final Collection<AbstractDecisionTree<T>> trees;

    public RandomForest(Builder<T> builder) {
        super(builder);
        this.trees = builder.trees;
        check();
    }

    public static <A extends FeatureData> Builder<A> builder() {
        return new Builder<>();
    }

    /**
     * Sanity checks for the random forest.
     * <p>
     * We check that:
     * <ul>
     * <li>there is at least a single tree in the forest</li>
     * </ul>
     */
    private void check() {
        if (trees.size() < 1) {
            String msg = "There must be at least a single tree in the forest";
            LOGGER.warn(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public int predict(T instance) {
        final DoubleMatrix proba = predictProba(instance);
        return classes[proba.argmax()];
    }

    @Override
    public DoubleMatrix predictProba(final T instance) {
        return trees.parallelStream() // why not, what the heck
                .map(tree -> tree.predictProba(instance))
                .reduce((left, right) -> left.add(right).div(2.)) // calculate mean
                .orElseThrow(() -> new RuntimeException("Cannot make predictions with no trees!"));
    }

    public static class Builder<A extends FeatureData> extends AbstractClassifier.Builder<Builder<A>> {

        private final Collection<AbstractDecisionTree<A>> trees = new ArrayList<>();

        private Builder() {
            // private no-op
        }

        public Builder<A> addTree(AbstractDecisionTree<A> tree) {
            this.trees.add(tree);
            return self();
        }

        public Builder<A> addTrees(Collection<AbstractDecisionTree<A>> trees) {
            this.trees.addAll(List.copyOf(trees));
            return self();
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        public RandomForest<A> build() {
            return new RandomForest<>(this);
        }
    }

}
