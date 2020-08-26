package org.monarchinitiative.squirls.core.classifier.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.classifier.Classifiable;
import org.monarchinitiative.squirls.core.classifier.TestBasedOnIrisInstances;
import org.monarchinitiative.squirls.core.classifier.TestTreeInstances;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

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
public class AbstractDecisionTreeTest extends TestBasedOnIrisInstances {

    private AbstractBinaryDecisionTree<Classifiable> tree;

    @BeforeEach
    public void setUp() {
        tree = TestTreeInstances.getTreeOne();
    }

    /**
     * The test compares outputs of {@link AbstractBinaryDecisionTree} with outputs of Scikit-learn's
     * <code>DecisionTreeClassifier</code> trained on Iris dataset as described below:
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
     * <p>
     * Predictions are made by executing:
     * <pre>
     * dtc.predict_proba(X[[50,54,100,104]])
     * dtc.predict(X[[50,54,100,104]])
     * </pre>
     * and this test compares output value of Java implementation with the output above.
     */
    @Test
    public void predictProba() {
        assertThat(tree.predictProba(versicolorOne), is(closeTo(0., EPSILON)));
        assertThat(tree.predictProba(versicolorFive), is(closeTo(0., EPSILON)));
        assertThat(tree.predictProba(virginicaOne), is(closeTo(1., EPSILON)));
        assertThat(tree.predictProba(virginicaFive), is(closeTo(1., EPSILON)));
    }

}