package org.monarchinitiative.threes.core.classifier.forest;

import org.monarchinitiative.threes.core.classifier.AbstractBinaryClassifier;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.tree.AbstractBinaryDecisionTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class uses a collection of {@link AbstractBinaryDecisionTree}s to perform classification of a {@link T} instance.
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
public class RandomForest<T extends FeatureData> extends AbstractBinaryClassifier<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomForest.class);

    private final Collection<AbstractBinaryDecisionTree<T>> trees;

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
    public Set<String> usedFeatureNames() {
        return trees.stream()
                .map(AbstractBinaryDecisionTree::usedFeatureNames)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public double predictProba(final T instance) {
        return trees.parallelStream() // why not, what the heck
                .mapToDouble(tree -> tree.predictProba(instance))
                .average()
                // this should not happen since we check for that in the constructor
                .orElseThrow(() -> new RuntimeException("Hoops, there is no tree in the forest!"));
    }

    public static class Builder<A extends FeatureData> extends AbstractBinaryClassifier.Builder<Builder<A>> {

        private final Collection<AbstractBinaryDecisionTree<A>> trees = new ArrayList<>();

        private Builder() {
            // private no-op
        }

        public Builder<A> addTree(AbstractBinaryDecisionTree<A> tree) {
            this.trees.add(tree);
            return self();
        }

        public Builder<A> addTrees(Collection<AbstractBinaryDecisionTree<A>> trees) {
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
