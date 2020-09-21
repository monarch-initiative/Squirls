package org.monarchinitiative.squirls.core.classifier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.classifier.io.Deserializer;
import org.monarchinitiative.squirls.core.classifier.io.OverallModelData;
import org.monarchinitiative.squirls.core.classifier.io.PipelineTransferModel;
import org.monarchinitiative.squirls.core.classifier.transform.feature.SplicingDataImputer;

import java.io.InputStream;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

class PipelineTest {

    private static final double EPSILON = 5E-12;

    private static PipelineTransferModel donorPipeModel;

    private Pipeline<Classifiable> pipeline;

    @BeforeAll
    static void beforeAll() throws Exception {
        OverallModelData overallModelData;
        try (InputStream is = Files.newInputStream(TestDataSourceConfig.SQUIRLS_MODEL_PATH)) {
            overallModelData = Deserializer.deserializeOverallModelData(is);
        }
        donorPipeModel = overallModelData.getDonorClf();
    }

    @BeforeEach
    void setUp() throws Exception {
        pipeline = Pipeline.builder()
                .transformer(new SplicingDataImputer<>(donorPipeModel.getFeatureNames(), donorPipeModel.getFeatureStatistics()))
                .classifier(Deserializer.deserializeDonorClassifier(donorPipeModel.getRf()))
                .build();
    }

    @Test
    void predictProba() throws Exception {
        final double pathoProba = pipeline.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(pathoProba, is(closeTo(.8594975603713706, EPSILON)));
    }
}