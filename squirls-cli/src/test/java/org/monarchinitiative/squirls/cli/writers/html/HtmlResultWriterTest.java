package org.monarchinitiative.squirls.cli.writers.html;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.cli.visualization.SplicingVariantGraphicsGenerator;
import org.monarchinitiative.squirls.cli.writers.*;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;


@SpringBootTest(classes = TestDataSourceConfig.class)
public class HtmlResultWriterTest {

    private static final Path OUTPATH = Paths.get("target/Sample192");

    private static OutputSettings OUTPUT_SETTINGS;

    @Autowired
    public SplicingPwmData splicingPwmData;

    @Autowired
    public JannovarData jannovarData;

    @Autowired
    public SplicingVariantGraphicsGenerator graphicsGenerator;

    private Set<WritableSplicingAllele> variantData;

    private HtmlResultWriter resultWriter;

    @BeforeAll
    public static void beforeAll() {
        OUTPUT_SETTINGS = new OutputSettings(OUTPATH.toString(), 100);
    }

    @BeforeEach
    public void setUp() throws Exception {
        VariantAnnotator annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        resultWriter = new HtmlResultWriter(graphicsGenerator);
        ReferenceDictionary rd = jannovarData.getRefDict();

        variantData = Set.of(
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
    }

    /**
     * This test does not currently test anything. It writes HTML file to {@link #OUTPATH}.
     *
     * @throws Exception if anything fails
     */
    @Test
    public void writeResults() throws Exception {
        AnalysisResults results = AnalysisResults.builder()
                .addAllSampleNames(List.of("Sample_192"))
                .analysisStats(new AnalysisStats(100, 120, 110))
                .settingsData(SettingsData.builder()
                        .inputPath("path/to/Sample_192.vcf")
                        .transcriptDb("refseq")
                        .build())
                .variants(variantData)
                .build();
        resultWriter.write(results, OUTPUT_SETTINGS);
    }
}