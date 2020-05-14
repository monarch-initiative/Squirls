package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.classifier.tree.AbstractDecisionTree;

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
     * dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(X,y)
     * </pre>
     *
     * @return tree
     */
    public static AbstractDecisionTree<FeatureData> getTreeOne() {
        // dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(X,y)
        return IrisDecisionTree.builder()
                .nNodes(9)
                .classes(List.of(0, 1, 2))
                .childrenLeft(List.of(1, -1, 3, 4, -1, -1, 7, -1, -1))
                .childrenRight(List.of(2, -1, 6, 5, -1, -1, 8, -1, -1))
                .thresholds(List.of(0.80000001, -2., 1.75, 4.95000005, -2., -2., 4.85000014, -2., -2.))
                .features(List.of(3, -2, 3, 2, -2, -2, 2, -2, -2))
                .values(List.of(
                        List.of(50, 50, 50),
                        List.of(50, 0, 0),
                        List.of(0, 50, 50),
                        List.of(0, 49, 5),
                        List.of(0, 47, 1),
                        List.of(0, 2, 4),
                        List.of(0, 1, 45),
                        List.of(0, 1, 2),
                        List.of(0, 0, 43)))
                .build();
    }

    /**
     * This is the tree <code>one</code> from the following code:
     * <pre>
     * from sklearn.ensemble import RandomForestClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * rfc = RandomForestClassifier(n_estimators=2, max_depth=2, random_state=10).fit(X, y)
     * one, two = rfc.estimators_
     * </pre>
     *
     * @return tree <code>one</code>
     */
    public static AbstractDecisionTree<FeatureData> getRandomForestTreeOne() {
        return IrisDecisionTree.builder()
                .nNodes(5)
                .classes(List.of(0, 1, 2))
                .childrenLeft(List.of(1, 2, -1, -1, -1))
                .childrenRight(List.of(4, 3, -1, -1, -1))
                .thresholds(List.of(1.75, 2.69999999, -2., -2., -2.))
                .features(List.of(3, 2, -2, -2, -2))
                .values(List.of(
                        List.of(46, 48, 56),
                        List.of(46, 48, 2),
                        List.of(46, 0, 0),
                        List.of(0, 48, 2),
                        List.of(0, 0, 54)))
                .build();
    }

    /**
     * This is the tree <code>two</code> from the following code:
     * <pre>
     * from sklearn.ensemble import RandomForestClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * rfc = RandomForestClassifier(n_estimators=2, max_depth=2, random_state=10).fit(X, y)
     * one, two = rfc.estimators_
     * </pre>
     *
     * @return tree <code>two</code>
     */
    public static AbstractDecisionTree<FeatureData> getRandomForestTreeTwo() {
        return IrisDecisionTree.builder()
                .nNodes(5)
                .classes(List.of(0, 1, 2))
                .childrenLeft(List.of(1, -1, 3, -1, -1))
                .childrenRight(List.of(2, -1, 4, -1, -1))
                .thresholds(List.of(.80000001, -2., 1.75, -2., -2.))
                .features(List.of(3, -2, 3, -2, -2))
                .values(List.of(
                        List.of(58, 45, 47),
                        List.of(58, 0, 0),
                        List.of(0, 45, 47),
                        List.of(0, 44, 3),
                        List.of(0, 1, 44)))
                .build();
    }
}
