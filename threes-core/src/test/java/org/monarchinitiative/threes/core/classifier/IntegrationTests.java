package org.monarchinitiative.threes.core.classifier;

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

    private static final double EPSILON = 5E-12;

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
        final DonorSplicingDecisionTree<Classifiable> tree = Deserializer.toDonorClassifierTree(List.of(0, 1)).apply(treeOne);

        // perform classification & assert
        double pathoProba = tree.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(pathoProba, is(closeTo(.9273255813953488, EPSILON)));

        pathoProba = tree.predictProba(TestVariantInstances.donorCryptic());
        assertThat(pathoProba, is(closeTo(.6923076923076923, EPSILON)));
    }

    @Test
    void donorTreeFiftyPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getDonorClf().getRf().getTrees().get(50);
        // make classifier
        final DonorSplicingDecisionTree<Classifiable> tree = Deserializer.toDonorClassifierTree(List.of(0, 1)).apply(treeOne);

        // perform classification & assert
        double pathoProba = tree.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(pathoProba, is(closeTo(.9156626506024096, EPSILON)));

        pathoProba = tree.predictProba(TestVariantInstances.donorCryptic());
        assertThat(pathoProba, is(closeTo(.09174311926605505, EPSILON)));
    }

    @Test
    void acceptorTreeZeroPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getAcceptorClf().getRf().getTrees().get(0);
        // make classifier
        final AcceptorSplicingDecisionTree<Classifiable> tree = Deserializer.toAcceptorClassifierTree(List.of(0, 1)).apply(treeOne);

        // perform classification & assert
        double pathoProba = tree.predictProba(TestVariantInstances.pathogenicAcceptor());
        assertThat(pathoProba, is(closeTo(.390625, EPSILON)));

        pathoProba = tree.predictProba(TestVariantInstances.acceptorCryptic());
        assertThat(pathoProba, is(closeTo(.14893617021276595, EPSILON)));
    }

    @Test
    void acceptorTreeFiftyPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getAcceptorClf().getRf().getTrees().get(50);
        // make classifier
        final AcceptorSplicingDecisionTree<Classifiable> tree = Deserializer.toAcceptorClassifierTree(List.of(0, 1)).apply(treeOne);

        // perform classification & assert
        double pathoProba = tree.predictProba(TestVariantInstances.pathogenicAcceptor());
        assertThat(pathoProba, is(closeTo(.603448275862069, EPSILON)));

        pathoProba = tree.predictProba(TestVariantInstances.acceptorCryptic());
        assertThat(pathoProba, is(closeTo(.027337289619612803, EPSILON)));
    }

    /*
    Tests at level of random forest.
     */

    @Test
    void donorForestPredictProba() {
        // get transfer format
        final RandomForestTransferModel rftm = overallModelData.getDonorClf().getRf();
        // make classifier
        final RandomForest<Classifiable> forest = Deserializer.deserializeDonorClassifier(rftm);

        double pathoProba = forest.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(pathoProba, is(closeTo(.7873663663768643, EPSILON)));

        pathoProba = forest.predictProba(TestVariantInstances.donorCryptic());
        assertThat(pathoProba, is(closeTo(.22439633436158474, EPSILON)));
    }

    @Test
    void acceptorForestPredictProba() {
        // get transfer format
        final RandomForestTransferModel rftm = overallModelData.getAcceptorClf().getRf();
        // make classifier
        final RandomForest<Classifiable> forest = Deserializer.deserializeAcceptorClassifier(rftm);

        double pathoProba = forest.predictProba(TestVariantInstances.pathogenicAcceptor());
        assertThat(pathoProba, is(closeTo(.3726891708713847, EPSILON)));

        pathoProba = forest.predictProba(TestVariantInstances.acceptorCryptic());
        assertThat(pathoProba, is(closeTo(.022480121825609604, EPSILON)));
    }

    /*
    Tests at level of classifier
     */

    @Test
    void ensembleClfPredictProba() throws Exception {
        final SquirlsClassifier overlord = Deserializer.deserialize(overallModelData);

        Prediction prediction = overlord.predict(TestVariantInstances.pathogenicDonor()).getPrediction();
        assertTrue(prediction.isPositive());
        assertThat(prediction.getMaxPathogenicity(), is(closeTo(.7873663663768643, EPSILON)));

        prediction = overlord.predict(TestVariantInstances.donorCryptic()).getPrediction();
        assertTrue(prediction.isPositive());
        assertThat(prediction.getMaxPathogenicity(), is(closeTo(.22439633436158474, EPSILON)));

        prediction = overlord.predict(TestVariantInstances.pathogenicAcceptor()).getPrediction();
        assertTrue(prediction.isPositive());
        assertThat(prediction.getMaxPathogenicity(), is(closeTo(.3726891708713847, EPSILON)));

        prediction = overlord.predict(TestVariantInstances.acceptorCryptic()).getPrediction();
        assertTrue(prediction.isPositive());
        assertThat(prediction.getMaxPathogenicity(), is(closeTo(.022480121825609604, EPSILON)));
    }
}
