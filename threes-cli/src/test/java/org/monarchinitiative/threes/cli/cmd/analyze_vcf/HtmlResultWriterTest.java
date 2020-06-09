package org.monarchinitiative.threes.cli.cmd.analyze_vcf;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.cli.PojosForTesting;
import org.monarchinitiative.threes.cli.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest(classes = TestDataSourceConfig.class)
class HtmlResultWriterTest {

    // TODO: 3. 6. 2020 remove
    private static final Path OUTPATH = Paths.get("/home/ielis/tmp/SQUIRLS.html");

    @Autowired
    private JannovarData jannovarData;

    private VariantAnnotator annotator;

    private HtmlResultWriter writer;

    @BeforeEach
    void setUp() {
        annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        writer = new HtmlResultWriter();
    }

    @Test
    void writeResults() throws Exception {
        final SplicingVariantAlleleEvaluation first = PojosForTesting.getDonorPlusFiveEvaluation(jannovarData.getRefDict(), annotator);
        final SplicingVariantAlleleEvaluation second = PojosForTesting.getAcceptorMinusOneEvaluation(jannovarData.getRefDict(), annotator);

        AnalysisResults results = AnalysisResults.builder()
                .addAllSampleNames(List.of("FAKE SAMPLE"))
                .analysisStats(AnalysisStats.builder()
                        .allVariants(100)
                        .alleleCount(120)
                        .annotatedAlleleCount(115)
                        .pathogenicAlleleCount(2)
                        .build())
                .settingsData(SettingsData.builder()
                        .inputPath("path to VCF").threshold(PojosForTesting.FAKE_THRESHOLD)
                        .transcriptDb("ENSEMBLah")
                        .build())
                .variantData(List.of(first, second))
                .build();
        try (OutputStream os = Files.newOutputStream(OUTPATH)) {
            writer.writeResults(os, results);
        }

    }
}