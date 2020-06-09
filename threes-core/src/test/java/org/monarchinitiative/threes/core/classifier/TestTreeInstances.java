package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.classifier.tree.AbstractBinaryDecisionTree;

import java.util.List;

/**
 * This class contains Python's Scikit-Learn DecisionTreeClassifier trees that were trained on IRIS dataset, as
 * described in individual Javadoc's descriptions.
 */
public class TestTreeInstances {

    /**
     * <pre>
     * from sklearn.tree import DecisionTreeClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * # only use the `versicolor` and `virginica` classes
     * Xbin = X[50:, :]
     * ybin = y[50:]
     * dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(Xbin,ybin)
     * </pre>
     *
     * @return tree
     */
    public static AbstractBinaryDecisionTree<Classifiable> getTreeOne() {
        // dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(Xbin, ybin)
        return IrisDecisionTree.builder()
                .nNodes(13)
                .classes(List.of(1, 2))
                .childrenLeft(List.of(1, 2, 3, -1, -1, 6, -1, -1, 9, 10, -1, -1, -1))
                .childrenRight(List.of(8, 5, 4, -1, -1, 7, -1, -1, 12, 11, -1, -1, -1))
                .thresholds(List.of(1.75, 4.95000005, 1.65000004, -2., -2., 1.58000001, -2., -2., 4.85000014, 3.10000002, -2., -2., -2.))
                .features(List.of(3, 2, 3, -2, -2, 3, -2, -2, 2, 1, -2, -2, -2))
                .values(List.of(
                        List.of(50, 50),
                        List.of(49, 5),
                        List.of(47, 1),
                        List.of(47, 0),
                        List.of(0, 1),
                        List.of(2, 4),
                        List.of(0, 3),
                        List.of(2, 1),
                        List.of(1, 45),
                        List.of(1, 2),
                        List.of(0, 2),
                        List.of(1, 0),
                        List.of(0, 43)))
                .build();
    }

    /**
     * This is the tree <code>one</code> from the following code:
     * <pre>
     * from sklearn.ensemble import RandomForestClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * # only use the `versicolor` and `virginica` classes
     * Xbin = X[:100, :]
     * ybin = y[:100]
     * rfc = RandomForestClassifier(n_estimators=2, max_depth=2, random_state=10).fit(Xbin, ybin)
     * one, two = rfc.estimators_
     * </pre>
     *
     * @return tree <code>one</code>
     */
    public static AbstractBinaryDecisionTree<Classifiable> getRandomForestTreeOne() {
        return IrisDecisionTree.builder()
                .nNodes(7)
                .classes(List.of(1, 2))
                .childrenLeft(List.of(1, 2, -1, -1, 5, -1, -1))
                .childrenRight(List.of(4, 3, -1, -1, 6, -1, -1))
                .thresholds(List.of(1.55000001, 4.95000005, -2., -2., 5.04999995, -2., -2.))
                .features(List.of(3, 2, -2, -2, 2, -2, -2))
                .values(List.of(
                        List.of(50, 50),
                        List.of(49, 3),
                        List.of(49, 0),
                        List.of(0, 3),
                        List.of(1, 47),
                        List.of(1, 10),
                        List.of(0, 37)))
                .build();
    }

    /**
     * This is the tree <code>two</code> from the following code:
     * <pre>
     * from sklearn.ensemble import RandomForestClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * # only use the `versicolor` and `virginica` classes
     * Xbin = X[:100, :]
     * ybin = y[:100]
     * rfc = RandomForestClassifier(n_estimators=2, max_depth=2, random_state=10).fit(Xbin, ybin)
     * one, two = rfc.estimators_
     * </pre>
     *
     * @return tree <code>two</code>
     */
    public static AbstractBinaryDecisionTree<Classifiable> getRandomForestTreeTwo() {
        return IrisDecisionTree.builder()
                .nNodes(7)
                .classes(List.of(1, 2))
                .childrenLeft(List.of(1, 2, -1, -1, 5, -1, -1))
                .childrenRight(List.of(4, 3, -1, -1, 6, -1, -1))
                .thresholds(List.of(1.69999999, 5.45000005, -2., -2., 1.84999996, -2., -2.))
                .features(List.of(3, 2, -2, -2, 3, -2, -2))
                .values(List.of(
                        List.of(52, 48),
                        List.of(50, 1),
                        List.of(50, 0),
                        List.of(0, 1),
                        List.of(2, 47),
                        List.of(2, 11),
                        List.of(0, 36)))
                .build();
    }
}
