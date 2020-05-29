package org.monarchinitiative.threes.core.classifier.io;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.Prediction;
import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.OverlordClassifier;
import org.monarchinitiative.threes.core.classifier.TestBasedOnIrisInstances;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeserializerTest extends TestBasedOnIrisInstances {

    private static final Path TOY_MODEL = Paths.get(DeserializerTest.class.getResource("example_model.yaml").getPath());
    private static final Path TOY_MODEL_v1_1 = Paths.get(DeserializerTest.class.getResource("example_model.v1.1.yaml").getPath());

    @Test
    void deserialize() throws Exception {
        final OverlordClassifier clf;
        try (InputStream inputStream = Files.newInputStream(TOY_MODEL)) {
            clf = Deserializer.deserialize(inputStream);
        }

        FeatureData instance = FeatureData.builder().addFeatures(Map.of(
                "donor_offset", 6.,
                "canonical_donor", 1.652939,
                "cryptic_donor", 0.,
                "acceptor_offset", 80.,
                "canonical_acceptor", 0.,
                "cryptic_acceptor", -9.765767,
                "phylop", 0.922,
                "hexamer", 1.696948,
                "septamer", 1.5778)).build();
        assertThat(clf, is(notNullValue()));

        final Prediction prediction = clf.predict(instance);
        assertTrue(prediction.isPathogenic());
        assertThat(prediction.getPathoProba(), is(closeTo(0.162997, 1E-5)));
    }

    @Test
    void deserializeModel_v1_1() throws Exception {
        final OverallModelData data;
        try (InputStream is = Files.newInputStream(TOY_MODEL_v1_1)) {
            data = Deserializer.deserializeOverallModelData(is);
        }
        assertThat(data, is(notNullValue()));

        // in addition to all other attributes of the v1 model, `v1.1` also has `intercept` and `slope` fields
        assertThat(data.getIntercept(), is(closeTo(-4.909676356421783, EPSILON)));
        assertThat(data.getSlope(), is(closeTo(13.648421772211595, EPSILON)));
    }

    @Test
    void deserializeOverallModelData() throws Exception {
        final OverallModelData data;
        try (InputStream is = Files.newInputStream(TOY_MODEL)) {
            data = Deserializer.deserializeOverallModelData(is);
        }
        assertThat(data, is(notNullValue()));

        assertThat(data.getDonorThreshold(), is(closeTo(.033530, 1e-5)));
        assertThat(data.getAcceptorThreshold(), is(closeTo(.018888, 1e-5)));

        final PipelineTransferModel donorPipe = data.getDonorClf();
        final RandomForestTransferModel donorClf = donorPipe.getRf();
        assertThat(donorClf.getClasses(), contains(0, 1));

        final Map<Integer, DecisionTreeTransferModel> donorTrees = donorClf.getTrees();
        assertThat(donorTrees.keySet(), hasItems(0, 1));
        assertThat(donorTrees.get(0), is(DecisionTreeTransferModel.builder()
                .childrenLeft(List.of(1, 2, 3, 4, 5, -1, 7, -1, -1, 10, 11, -1, -1, 14, -1, -1, 17, 18, -1, 20, -1, -1, -1, 24, 25, -1, 27, 28, -1, -1, -1, -1, 33, 34, 35, 36, 37, -1, -1, -1, 41, 42, -1, -1, 45, -1, -1, 48, 49, 50, -1, -1, 53, -1, -1, 56, 57, -1, -1, 60, -1, -1, 63, 64, 65, 66, -1, -1, 69, -1, -1, 72, 73, -1, -1, -1, 77, 78, 79, -1, -1, -1, -1))
                .childrenRight(List.of(32, 23, 16, 9, 6, -1, 8, -1, -1, 13, 12, -1, -1, 15, -1, -1, 22, 19, -1, 21, -1, -1, -1, 31, 26, -1, 30, 29, -1, -1, -1, -1, 62, 47, 40, 39, 38, -1, -1, -1, 44, 43, -1, -1, 46, -1, -1, 55, 52, 51, -1, -1, 54, -1, -1, 59, 58, -1, -1, 61, -1, -1, 76, 71, 68, 67, -1, -1, 70, -1, -1, 75, 74, -1, -1, -1, 82, 81, 80, -1, -1, -1, -1))
                .feature(List.of(2, 3, 3, 5, 3, -2, 4, -2, -2, 5, 4, -2, -2, 0, -2, -2, 2, 3, -2, 5, -2, -2, -2, 0, 3, -2, 2, 5, -2, -2, -2, -2, 1, 1, 0, 3, 5, -2, -2, -2, 0, 3, -2, -2, 5, -2, -2, 1, 5, 4, -2, -2, 3, -2, -2, 3, 2, -2, -2, 3, -2, -2, 1, 1, 3, 4, -2, -2, 3, -2, -2, 1, 3, -2, -2, -2, 0, 2, 5, -2, -2, -2, -2))
                .nodeCount(83)
                .threshold(List.of(-0.0023636077530682087, 7.2799999713897705, 1.3084999918937683, -2.6184500455856323, -4.18149995803833, -2.0, -2.8303319215774536, -2.0, -2.0, -1.2245000004768372, -1.7611680626869202, -2.0, -2.0, 13.5, -2.0, -2.0, -0.13510171324014664, 1.309499979019165, -2.0, -1.0116499662399292, -2.0, -2.0, -2.0, -3.5, 7.288500070571899, -2.0, -3.43417751789093, -1.9275000095367432, -2.0, -2.0, -2.0, -2.0, 2.466026186943054, 0.6139303147792816, -3.5, 7.855000019073486, -0.7427499890327454, -2.0, -2.0, -2.0, 15.5, 7.734999895095825, -2.0, -2.0, -1.6208999752998352, -2.0, -2.0, 0.8261631429195404, 0.9111500084400177, -0.16164140310138464, -2.0, -2.0, 3.11299991607666, -2.0, -2.0, 3.0364999771118164, 0.7663134559988976, -2.0, -2.0, 8.055500030517578, -2.0, -2.0, 3.4985283613204956, 3.0687578916549683, 1.7764999866485596, -1.480242133140564, -2.0, -2.0, 4.489000082015991, -2.0, -2.0, 3.34597384929657, 6.203000068664551, -2.0, -2.0, -2.0, 4.5, 5.562227487564087, -1.744599997997284, -2.0, -2.0, -2.0, -2.0))
                .values(List.of(
                        List.of(41970, 1472),
                        List.of(39619, 240),
                        List.of(38947, 186),
                        List.of(31600, 104),
                        List.of(152, 7),
                        List.of(5, 3),
                        List.of(147, 4),
                        List.of(87, 4),
                        List.of(60, 0),
                        List.of(31448, 97),
                        List.of(3406, 33),
                        List.of(1732, 29),
                        List.of(1674, 4),
                        List.of(28042, 64),
                        List.of(22177, 41),
                        List.of(5865, 23),
                        List.of(7347, 82),
                        List.of(7347, 79),
                        List.of(6, 4),
                        List.of(7341, 75),
                        List.of(1213, 36),
                        List.of(6128, 39),
                        List.of(0, 3),
                        List.of(672, 54),
                        List.of(665, 46),
                        List.of(0, 2),
                        List.of(665, 44),
                        List.of(635, 35),
                        List.of(5, 5),
                        List.of(630, 30),
                        List.of(30, 9),
                        List.of(7, 8),
                        List.of(2351, 1232),
                        List.of(2078, 331),
                        List.of(1679, 117),
                        List.of(283, 67),
                        List.of(279, 57),
                        List.of(128, 47),
                        List.of(151, 10),
                        List.of(4, 10),
                        List.of(1396, 50),
                        List.of(1299, 35),
                        List.of(1288, 32),
                        List.of(11, 3),
                        List.of(97, 15),
                        List.of(4, 9),
                        List.of(93, 6),
                        List.of(399, 214),
                        List.of(82, 121),
                        List.of(16, 68),
                        List.of(4, 3),
                        List.of(12, 65),
                        List.of(66, 53),
                        List.of(60, 34),
                        List.of(6, 19),
                        List.of(317, 93),
                        List.of(251, 30),
                        List.of(247, 22),
                        List.of(4, 8),
                        List.of(66, 63),
                        List.of(66, 50),
                        List.of(0, 13),
                        List.of(273, 901),
                        List.of(221, 572),
                        List.of(119, 505),
                        List.of(58, 58),
                        List.of(6, 19),
                        List.of(52, 39),
                        List.of(61, 447),
                        List.of(33, 155),
                        List.of(28, 292),
                        List.of(102, 67),
                        List.of(88, 37),
                        List.of(85, 22),
                        List.of(3, 15),
                        List.of(14, 30),
                        List.of(52, 329),
                        List.of(36, 320),
                        List.of(31, 319),
                        List.of(6, 0),
                        List.of(25, 319),
                        List.of(5, 1),
                        List.of(16, 9)))
                .build()));
    }
}