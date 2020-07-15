package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@SpringBootTest(classes = TestDataSourceConfig.class)
class HtmlResultWriterTest {

    // TODO: 3. 6. 2020 remove
    private static final Path OUTPATH = Paths.get("SQUIRLS.html");

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
        final ReferenceDictionary rd = jannovarData.getRefDict();
        Set<SplicingVariantAlleleEvaluation> variantData = Set.of(
                // TODO: 7. 7. 2020 consider removing
//                VariantsForTesting.SURF2DonorExon3Plus4Evaluation(rd, annotator),
//                VariantsForTesting.SURF2Exon3AcceptorMinus2Evaluation(rd, annotator),
                // donor
                VariantsForTesting.BRCA2DonorExon15plus2QUID(rd, annotator),
                VariantsForTesting.ALPLDonorExon7Minus2(rd, annotator),
                VariantsForTesting.HBBcodingExon1UpstreamCrypticInCanonical(rd, annotator),
                VariantsForTesting.HBBcodingExon1UpstreamCryptic(rd, annotator),
                // acceptor
                VariantsForTesting.VWFAcceptorExon26minus2QUID(rd, annotator),
                VariantsForTesting.TSC2AcceptorExon11Minus3(rd, annotator),
                VariantsForTesting.COL4A5AcceptorExon11Minus8(rd, annotator),
                VariantsForTesting.RYR1codingExon102crypticAcceptor(rd, annotator),
                // SRE
                VariantsForTesting.NF1codingExon9coding_SRE(rd, annotator)
        );

        AnalysisResults results = AnalysisResults.builder()
                .addAllSampleNames(List.of("Sample_192"))
                .analysisStats(AnalysisStats.builder()
                        .allVariants(100)
                        .alleleCount(120)
                        .annotatedAlleleCount(115)
                        .pathogenicAlleleCount(2)
                        .build())
                .settingsData(SettingsData.builder()
                        .inputPath("path/to/Sample_192.vcf")
                        .threshold(VariantsForTesting.FAKE_THRESHOLD)
                        .transcriptDb("refseq")
                        .build())
                .variantData(variantData)
                .build();
        try (OutputStream os = Files.newOutputStream(OUTPATH)) {
            writer.writeResults(os, results);
        }

    }
}