package org.monarchinitiative.threes.core.classifier.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.TestBasedOnIrisInstances;
import org.monarchinitiative.threes.core.classifier.TestTreeInstances;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test suite tests if the decision tree works at all. The tests compare predictions of Python's Scikit-Learn
 * DecisionTreeClassifier that was trained on IRIS dataset as:
 * <pre>
 * from sklearn.tree import DecisionTreeClassifier
 * from sklearn.datasets import load_iris
 *
 * X, y = load_iris(return_X_y=True)
 * dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(X,y)
 * </pre>
 * <p>
 * Then we get predictions as:
 * <pre>
 * dtc.predict_proba(X[[0,4,50,54,100,104]])
 * dtc.predict(X[[0,4,50,54,100,104]])
 * </pre>
 * and the test compares output value of Java implementation with the output above.
 */
class AbstractDecisionTreeTest extends TestBasedOnIrisInstances {

    private AbstractDecisionTree<FeatureData> tree;

    @BeforeEach
    void setUp() {
        tree = TestTreeInstances.getTreeOne();
    }

    /**
     * The test compares outputs of {@link AbstractDecisionTree} with outputs of Scikit-learn's
     * <code>DecisionTreeClassifier</code> trained on Iris dataset as described below:
     * <pre>
     * from sklearn.tree import DecisionTreeClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(X,y)
     * </pre>
     * <p>
     * Predictions are made by executing:
     * <pre>
     * dtc.predict_proba(X[[0,4,50,54,100,104]])
     * dtc.predict(X[[0,4,50,54,100,104]])
     * </pre>
     * and this test compares output value of Java implementation with the output above.
     */
    @Test
    void predictProba() {
        assertThat(tree.predictProba(setosaOne).toArray(), is(new double[]{1.000, 0.000, 0.}));
        assertThat(tree.predictProba(setosaFive).toArray(), is(new double[]{1.0, 0., 0.}));
        assertThat(tree.predictProba(versicolorOne).toArray(), is(new double[]{0., .9791666666666666, .020833333333333332}));
        assertThat(tree.predictProba(versicolorFive).toArray(), is(new double[]{0., .9791666666666666, .020833333333333332}));
        assertThat(tree.predictProba(virginicaOne).toArray(), is(new double[]{0., 0., 1.}));
        assertThat(tree.predictProba(virginicaFive).toArray(), is(new double[]{0., 0., 1.}));
    }

    /**
     * Similarly to the {@link #predictProba()}, this test compares outputs of {@link AbstractDecisionTree} with
     * outputs of Scikit-learn's <code>DecisionTreeClassifier</code> trained on Iris dataset as described below:
     * <pre>
     * from sklearn.tree import DecisionTreeClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(X,y)
     * </pre>
     * <p>
     * Predictions are made by executing:
     * <pre>
     * dtc.predict_proba(X[[0,4,50,54,100,104]])
     * dtc.predict(X[[0,4,50,54,100,104]])
     * </pre>
     * and this test compares output value of Java implementation with the output above.
     */
    @Test
    void predict() {
        assertThat(tree.predict(setosaOne), is(0));
        assertThat(tree.predict(setosaFive), is(0));
        assertThat(tree.predict(versicolorOne), is(1));
        assertThat(tree.predict(versicolorFive), is(1));
        assertThat(tree.predict(virginicaOne), is(2));
        assertThat(tree.predict(virginicaFive), is(2));
    }

}