package org.monarchinitiative.squirls.core.classifier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.classifier.forest.RandomForest;
import org.monarchinitiative.squirls.core.classifier.io.DecisionTreeTransferModel;
import org.monarchinitiative.squirls.core.classifier.io.Deserializer;
import org.monarchinitiative.squirls.core.classifier.io.OverallModelData;
import org.monarchinitiative.squirls.core.classifier.io.PipelineTransferModel;
import org.monarchinitiative.squirls.core.classifier.tree.BinaryDecisionTree;

import java.io.InputStream;
import java.nio.file.Files;

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

    private static OverallModelData overallModelData;

    @BeforeAll
    static void beforeAll() throws Exception {
        try (InputStream is = Files.newInputStream(TestDataSourceConfig.SQUIRLS_MODEL_PATH)) {
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
        final BinaryDecisionTree<Classifiable> tree = Deserializer.toDonorClassifierTree(overallModelData.getDonorClf()).apply(treeOne);

        // perform classification & assert
        double pathoProba = tree.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(pathoProba, is(closeTo(0., EPSILON)));

        pathoProba = tree.predictProba(TestVariantInstances.donorCryptic());
        assertThat(pathoProba, is(closeTo(.6545454545454545, EPSILON)));
    }

    @Test
    void donorTreeFiftyPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getDonorClf().getRf().getTrees().get(50);
        // make classifier
        final BinaryDecisionTree<Classifiable> tree = Deserializer.toDonorClassifierTree(overallModelData.getDonorClf()).apply(treeOne);

        // perform classification & assert
        double pathoProba = tree.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(pathoProba, is(closeTo(.9301075268817204, EPSILON)));

        pathoProba = tree.predictProba(TestVariantInstances.donorCryptic());
        assertThat(pathoProba, is(closeTo(.034782608695652174, EPSILON)));
    }

    @Test
    void acceptorTreeZeroPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getAcceptorClf().getRf().getTrees().get(0);
        // make classifier
        final BinaryDecisionTree<Classifiable> tree = Deserializer.toAcceptorClassifierTree(overallModelData.getAcceptorClf()).apply(treeOne);

        // perform classification & assert
        double pathoProba = tree.predictProba(TestVariantInstances.pathogenicAcceptor());
        assertThat(pathoProba, is(closeTo(.5925925925925926, EPSILON)));

        pathoProba = tree.predictProba(TestVariantInstances.acceptorCryptic());
        assertThat(pathoProba, is(closeTo(.04947229551451187, EPSILON)));
    }

    @Test
    void acceptorTreeFiftyPredictProba() {
        // get transfer format
        final DecisionTreeTransferModel treeOne = overallModelData.getAcceptorClf().getRf().getTrees().get(50);
        // make classifier
        final BinaryDecisionTree<Classifiable> tree = Deserializer.toAcceptorClassifierTree(overallModelData.getAcceptorClf()).apply(treeOne);

        // perform classification & assert
        double pathoProba = tree.predictProba(TestVariantInstances.pathogenicAcceptor());
        assertThat(pathoProba, is(closeTo(.4111111111111111, EPSILON)));

        pathoProba = tree.predictProba(TestVariantInstances.acceptorCryptic());
        assertThat(pathoProba, is(closeTo(.017110078132726733, EPSILON)));
    }

    /*
    Tests at level of random forest.
     */

    @Test
    void donorForestPredictProba() {
        // get transfer format
        final PipelineTransferModel ptm = overallModelData.getDonorClf();
        // make classifier
        final RandomForest<Classifiable> forest = Deserializer.deserializeDonorClassifier(ptm);

        double pathoProba = forest.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(pathoProba, is(closeTo(.8594975603713706, EPSILON)));

        pathoProba = forest.predictProba(TestVariantInstances.donorCryptic());
        assertThat(pathoProba, is(closeTo(.21285603989235405, EPSILON)));
    }

    @Test
    void acceptorForestPredictProba() {
        // get transfer format
        final PipelineTransferModel ptm = overallModelData.getAcceptorClf();
        // make classifier
        final RandomForest<Classifiable> forest = Deserializer.deserializeAcceptorClassifier(ptm);

        double pathoProba = forest.predictProba(TestVariantInstances.pathogenicAcceptor());
        assertThat(pathoProba, is(closeTo(.4936002946815444, EPSILON)));

        pathoProba = forest.predictProba(TestVariantInstances.acceptorCryptic());
        assertThat(pathoProba, is(closeTo(.01954726418268275, EPSILON)));
    }

    /*
    Tests at level of classifier
     */

    @Test
    void ensembleClfPredictProba() throws Exception {
        final SquirlsClassifier overlord = Deserializer.deserialize(overallModelData);

        Prediction prediction = overlord.predict(TestVariantInstances.pathogenicDonor()).getPrediction();
        assertTrue(prediction.isPositive());
        assertThat(prediction.getMaxPathogenicity(), is(closeTo(.8594975603713706, EPSILON)));

        prediction = overlord.predict(TestVariantInstances.donorCryptic()).getPrediction();
        assertTrue(prediction.isPositive());
        assertThat(prediction.getMaxPathogenicity(), is(closeTo(.21285603989235405, EPSILON)));

        prediction = overlord.predict(TestVariantInstances.pathogenicAcceptor()).getPrediction();
        assertTrue(prediction.isPositive());
        assertThat(prediction.getMaxPathogenicity(), is(closeTo(.4936002946815444, EPSILON)));

        prediction = overlord.predict(TestVariantInstances.acceptorCryptic()).getPrediction();
        assertTrue(prediction.isPositive());
        assertThat(prediction.getMaxPathogenicity(), is(closeTo(.01954726418268275, EPSILON)));
    }
}
