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
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
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
                .name("pipeline_star")
                .classes(List.of(1, 2))
                .transformer(new SplicingDataImputer<>(donorPipeModel.getFeatureNames(), donorPipeModel.getFeatureStatistics()))
                .classifier(Deserializer.deserializeDonorClassifier(donorPipeModel))
                .build();
    }

    @Test
    void predictProba() throws Exception {
        final double pathoProba = pipeline.predictProba(TestVariantInstances.pathogenicDonor());
        assertThat(pathoProba, is(closeTo(.8594975603713706, EPSILON)));
    }

    @Test
    public void pipelineGetFeatureIndices() {
        final Map<Integer, String> featureIndices = donorPipeModel.getFeatureIndices();

        assertThat(featureIndices, allOf(
                hasEntry(0, "donor_offset"),
                hasEntry(1, "canonical_donor"),
                hasEntry(2, "cryptic_donor"),
                hasEntry(3, "phylop"),
                hasEntry(4, "hexamer"),
                hasEntry(5, "septamer")));
    }

    @Test
    public void pipelineName() {
        assertThat(pipeline.getName(), is("pipeline_star"));
    }
}