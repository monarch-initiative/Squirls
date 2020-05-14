package org.monarchinitiative.threes.core.classifier.io;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.OverlordClassifier;
import org.monarchinitiative.threes.core.classifier.TestBasedOnIrisInstances;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;

class DeserializerTest extends TestBasedOnIrisInstances {

    private static final Path TOY_MODEL = Paths.get(DeserializerTest.class.getResource("toy_model.yaml").getPath());

    @Disabled
    // use real model for testing of this function
    // TODO - continue!
    @Test
    void deserialize() throws Exception {
        final OverlordClassifier clf;
        try (InputStream inputStream = Files.newInputStream(TOY_MODEL)) {
            clf = Deserializer.deserialize(inputStream);
        }

        assertThat(clf, is(notNullValue()));

        final int prediction = clf.predict(setosaOne);

    }

    @Test
    void deserializeOverallModelData() throws Exception {
        final OverallModelData data;
        try (InputStream is = Files.newInputStream(TOY_MODEL)) {
            data = Deserializer.deserializeOverallModelData(is);
        }
        assertThat(data, is(notNullValue()));

        assertThat(data.getDonorThreshold(), is(closeTo(.333333, 1e-5)));
        assertThat(data.getAcceptorThreshold(), is(closeTo(.666666, 1e-5)));

        final RandomForestTransferModel donorClf = data.getDonorClf();
        assertThat(donorClf.getClasses(), contains(0, 1, 2));

        final Map<Integer, DecisionTreeTransferModel> donorTrees = donorClf.getTrees();
        assertThat(donorTrees.keySet(), contains(0, 1));
        assertThat(donorTrees.get(0), is(DecisionTreeTransferModel.builder()
                .childrenLeft(List.of(1, 2, -1, -1, -1))
                .childrenRight(List.of(4, 3, -1, -1, -1))
                .feature(List.of(3, 2, -2, -2, -2))
                .nodeCount(5)
                .threshold(List.of(1.75, 2.699999988079071, -2., -2., -2.))
                .values(List.of(
                        List.of(46, 48, 56),
                        List.of(46, 48, 2),
                        List.of(46, 0, 0),
                        List.of(0, 48, 2),
                        List.of(0, 0, 54)))
                .build()));
        assertThat(donorTrees.get(1), is(DecisionTreeTransferModel.builder()
                .childrenLeft(List.of(1, -1, 3, -1, -1))
                .childrenRight(List.of(2, -1, 4, -1, -1))
                .feature(List.of(3, -2, 3, -2, -2))
                .nodeCount(5)
                .threshold(List.of(.800000011920929, -2., 1.75, -2., -2.))
                .values(List.of(
                        List.of(58, 45, 47),
                        List.of(58, 0, 0),
                        List.of(0, 45, 47),
                        List.of(0, 44, 3),
                        List.of(0, 1, 44)))
                .build()));

        final RandomForestTransferModel acceptorClf = data.getAcceptorClf();
        assertThat(acceptorClf.getClasses(), contains(0, 1, 2));

        final Map<Integer, DecisionTreeTransferModel> acceptorTrees = acceptorClf.getTrees();
        assertThat(acceptorTrees.keySet(), contains(0, 1));
        assertThat(acceptorTrees.get(0), is(DecisionTreeTransferModel.builder()
                .childrenLeft(List.of(1, 2, -1, -1, 5, -1, -1))
                .childrenRight(List.of(4, 3, -1, -1, 6, -1, -1))
                .feature(List.of(0, 3, -2, -2, 2, -2, -2))
                .nodeCount(7)
                .threshold(List.of(5.450000047683716, 0.7000000029802322, -2., -2., 4.75, -2., -2.))
                .values(List.of(
                        List.of(54, 56, 40),
                        List.of(48, 7, 2),
                        List.of(48, 0, 0),
                        List.of(0, 7, 2),
                        List.of(6, 49, 38),
                        List.of(6, 45, 0),
                        List.of(0, 4, 38)))
                .build()));
        assertThat(acceptorTrees.get(1), is(DecisionTreeTransferModel.builder()
                .childrenLeft(List.of(1, -1, 3, -1, -1))
                .childrenRight(List.of(2, -1, 4, -1, -1))
                .feature(List.of(2, -2, 3, -2, -2))
                .nodeCount(5)
                .threshold(List.of(2.449999988079071, -2., 1.75, -2., -2.))
                .values(List.of(
                        List.of(43, 51, 56),
                        List.of(43, 0, 0),
                        List.of(0, 51, 56),
                        List.of(0, 50, 6),
                        List.of(0, 1, 50)))
                .build()));
    }
}