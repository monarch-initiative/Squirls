package org.monarchinitiative.threes.core.classifier;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.classifier.impute.SplicingDataImputer;
import org.monarchinitiative.threes.core.classifier.io.Deserializer;
import org.monarchinitiative.threes.core.classifier.io.OverallModelData;
import org.monarchinitiative.threes.core.classifier.io.PipelineTransferModel;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineTest {

    private static final String TOY_MODEL_PATH = "io/example_model.yaml";

    private static PipelineTransferModel donorPipeModel;

    private Pipeline<FeatureData> pipeline;

    @BeforeAll
    static void beforeAll() throws Exception {
        OverallModelData overallModelData;
        try (InputStream is = PipelineTest.class.getResourceAsStream(TOY_MODEL_PATH)) {
            overallModelData = Deserializer.deserializeOverallModelData(is);
        }
        donorPipeModel = overallModelData.getDonorClf();
    }

    @BeforeEach
    void setUp() throws Exception {
        pipeline = Pipeline.builder()
                .transformer(new SplicingDataImputer(donorPipeModel.getFeatureNames(), donorPipeModel.getFeatureStatistics()))
                .classifier(Deserializer.deserializeDonorClassifier(donorPipeModel.getRf()))
                .build();
    }

    @Test
    void predict() throws Exception {
        final int predict = pipeline.predict(TestVariantInstances.pathogenicDonor());
        assertThat(predict, is(1));
    }

    @Test
    void predictProba() throws Exception {
        final DoubleMatrix predict = pipeline.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(predict.toArray(), is(new double[]{.21263363362313573, .7873663663768643}));
    }
}