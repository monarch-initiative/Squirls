package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.Prediction;
import org.monarchinitiative.threes.core.classifier.forest.RandomForest;
import org.monarchinitiative.threes.core.classifier.io.DecisionTreeTransferModel;
import org.monarchinitiative.threes.core.classifier.io.Deserializer;
import org.monarchinitiative.threes.core.classifier.io.OverallModelData;
import org.monarchinitiative.threes.core.classifier.io.RandomForestTransferModel;
import org.monarchinitiative.threes.core.classifier.tree.AcceptorSplicingDecisionTree;
import org.monarchinitiative.threes.core.classifier.tree.DonorSplicingDecisionTree;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These tests assume that deserialization works and test predicted probabilities and classes for selected data points
 * with expected values with high precision.
 * <p>
 * The reference values came from Jupyter notebook, however they are not available and they should be regarded as
 * ground truth.
 */
public class IntegrationTests {

    private static final String TOY_MODEL_PATH = "io/example_model.yaml";

    private static OverallModelData overallModelData;

    @BeforeAll
    static void beforeAll() throws Exception {
        try (InputStream is = IntegrationTests.class.getResourceAsStream(TOY_MODEL_PATH)) {
            overallModelData = Deserializer.deserializeOverallModelData(is);
        }
    }

    /*
    Tests at level of trees.
     */

    @Test
    void donorTreeZeroPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getDonorClf().getRf().getTrees().get(0);
        // make classifier
        final DonorSplicingDecisionTree tree = Deserializer.toDonorClassifierTree(List.of(0, 1)).apply(treeOne);

        // perform classification & assert
        DoubleMatrix doubleMatrix = tree.predictProba(TestVariantInstances.pathogenicDonor()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.07267441860465117, .9273255813953488}));

        doubleMatrix = tree.predictProba(TestVariantInstances.donorCryptic()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.3076923076923077, .6923076923076923}));
    }

    @Test
    void donorTreeFiftyPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getDonorClf().getRf().getTrees().get(50);
        // make classifier
        final DonorSplicingDecisionTree tree = Deserializer.toDonorClassifierTree(List.of(0, 1)).apply(treeOne);

        // perform classification & assert
        DoubleMatrix doubleMatrix = tree.predictProba(TestVariantInstances.pathogenicDonor()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.08433734939759036, .9156626506024096}));

        doubleMatrix = tree.predictProba(TestVariantInstances.donorCryptic()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.908256880733945, .09174311926605505}));
    }

    @Test
    void acceptorTreeZeroPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getAcceptorClf().getRf().getTrees().get(0);
        // make classifier
        final AcceptorSplicingDecisionTree tree = Deserializer.toAcceptorClassifierTree(List.of(0, 1)).apply(treeOne);

        // perform classification & assert
        DoubleMatrix doubleMatrix = tree.predictProba(TestVariantInstances.pathogenicAcceptor()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.609375, .390625}));

        doubleMatrix = tree.predictProba(TestVariantInstances.acceptorCryptic()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.851063829787234, .14893617021276595}));
    }

    @Test
    void acceptorTreeFiftyPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getAcceptorClf().getRf().getTrees().get(50);
        // make classifier
        final AcceptorSplicingDecisionTree tree = Deserializer.toAcceptorClassifierTree(List.of(0, 1)).apply(treeOne);

        // perform classification & assert
        DoubleMatrix doubleMatrix = tree.predictProba(TestVariantInstances.pathogenicAcceptor()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.39655172413793105, .603448275862069}));

        doubleMatrix = tree.predictProba(TestVariantInstances.acceptorCryptic()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.9726627103803872, .027337289619612803}));
    }

    /*
    Tests at level of random forest.
     */

    @Test
    void donorForestPredictProba() {
        // get transfer format
        final RandomForestTransferModel rftm = overallModelData.getDonorClf().getRf();
        // make classifier
        final RandomForest<FeatureData> forest = Deserializer.deserializeDonorClassifier(rftm);

        DoubleMatrix doubleMatrix = forest.predictProba(TestVariantInstances.pathogenicDonor()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.21263363362313573, .7873663663768643}));

        doubleMatrix = forest.predictProba(TestVariantInstances.donorCryptic()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.7756036656384151, .22439633436158474}));
    }

    @Test
    void acceptorForestPredictProba() {
        // get transfer format
        final RandomForestTransferModel rftm = overallModelData.getAcceptorClf().getRf();
        // make classifier
        final RandomForest<FeatureData> forest = Deserializer.deserializeAcceptorClassifier(rftm);

        DoubleMatrix doubleMatrix = forest.predictProba(TestVariantInstances.pathogenicAcceptor()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.6273108291286152, .3726891708713847}));

        doubleMatrix = forest.predictProba(TestVariantInstances.acceptorCryptic()).transpose();
        assertThat(doubleMatrix.toArray(), is(new double[]{.9775198781743903, .022480121825609604}));
    }

    /*
    Tests at level of classifier
     */
    @Test
    void ensembleClfPredictProba() throws Exception {
        final OverlordClassifier overlord = Deserializer.deserialize(overallModelData);

        Prediction prediction = overlord.predict(TestVariantInstances.pathogenicDonor());
        assertTrue(prediction.isPathogenic());
        assertThat(prediction.getPathoProba(), is(closeTo(.7873663663768643, 1E-5)));
//        DoubleMatrix doubleMatrix = overlord.predictProba(TestVariantInstances.pathogenicDonor()).transpose();
//        assertThat(doubleMatrix.toArray(), is(new double[]{.2126336336231357, .7873663663768643}));

        prediction = overlord.predict(TestVariantInstances.donorCryptic());
        assertTrue(prediction.isPathogenic());
        assertThat(prediction.getPathoProba(), is(closeTo(.22439633436158474, 1E-5)));
//        assertThat(doubleMatrix.toArray(), is(new double[]{.7756036656384153, .22439633436158474}));

        prediction = overlord.predict(TestVariantInstances.pathogenicAcceptor());
        assertTrue(prediction.isPathogenic());
        assertThat(prediction.getPathoProba(), is(closeTo(.3726891708713847, 1E-5)));
//        assertThat(doubleMatrix.toArray(), is(new double[]{.6273108291286154, .3726891708713847}));

        prediction = overlord.predict(TestVariantInstances.acceptorCryptic());
        assertTrue(prediction.isPathogenic());
        assertThat(prediction.getPathoProba(), is(closeTo(.022480121825609604, 1E-5)));
//        assertThat(doubleMatrix.toArray(), is(new double[]{.9775198781743903, .022480121825609604}));
    }
}
