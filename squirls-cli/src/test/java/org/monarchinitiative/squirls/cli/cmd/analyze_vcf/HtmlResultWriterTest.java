package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.AnalysisResults;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.AnalysisStats;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.SettingsData;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootTest(classes = TestDataSourceConfig.class)
class HtmlResultWriterTest {

    private static final Path OUTPATH = Paths.get("target/SQUIRLS.html");

    @Autowired
    public SplicingPwmData splicingPwmData;

    @Autowired
    public JannovarData jannovarData;

    private Set<PresentableVariant> variantData;

    private HtmlResultWriter writer;

    private static Function<SplicingVariantAlleleEvaluation, PresentableVariant> toPresentableVariant() {
        return ve -> PresentableVariant.of(ve.getRepresentation(),
                ve.getAnnotations().getHighestImpactAnnotation().getGeneSymbol(),
                ve.getMaxScore(),
                ve.getGraphics());
    }

    @BeforeEach
    void setUp() throws Exception {
        VariantAnnotator annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        writer = new HtmlResultWriter();
        final ReferenceDictionary rd = jannovarData.getRefDict();

        variantData = Set.of(
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
        ).stream()
                .map(toPresentableVariant()) // TODO - the test data should be presentable by default
                .collect(Collectors.toSet());

    }

    /**
     * This test does not currently test anything. It writes HTML file to {@link #OUTPATH}.
     *
     * @throws Exception if anything fails
     */
    @Test
    void writeResults() throws Exception {
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
                .variants(variantData)
                .build();
        try (OutputStream os = Files.newOutputStream(OUTPATH)) {
            writer.writeResults(os, results);
        }
    }

    /**
     * This test does not currently test anything. It writes HTML file to {@link #OUTPATH}.
     *
     * @throws Exception bla
     */
    @Test
    void writeResultsRealGraphics() throws Exception {
        // TODO - remove or make a real test
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
                .variants(variantData)
                .build();
        try (OutputStream os = Files.newOutputStream(OUTPATH)) {
            writer.writeResults(os, results);
        }
    }
}