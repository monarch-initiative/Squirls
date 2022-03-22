/*
 * SOFTWARE LICENSE AGREEMENT
 * FOR NON-COMMERCIAL USE
 * 	This Software License Agreement (this “Agreement”) is made between you (“You,” “Your,” or “Licensee”) and The
 * 	Jackson Laboratory (“Licensor”). This Agreement grants to You a license to the Licensed Software subject to Your
 * 	acceptance of all the terms and conditions contained in this Agreement. Please read the terms and conditions
 * 	carefully. You accept the terms and conditions set forth herein by using, downloading or opening the software
 *
 * 1. LICENSE
 *
 * 1.1	Grant. Subject to the terms and conditions of this Agreement, Licensor hereby grants to Licensee a worldwide,
 * royalty-free, non-exclusive, non-transferable, non-sublicensable license to download, copy, display, and use the
 * Licensed Software for Non-Commercial purposes only. “Licensed Software” means the current version of the software.
 * “Non-Commercial” means not intended or directed toward commercial advantage or monetary compensation.
 *
 * 1.2	License Limitations. Nothing in this Agreement shall be construed to confer any rights upon Licensee except as
 * expressly granted herein. Licensee may not use or exploit the Licensed Software other than expressly permitted by this
 * Agreement. Licensee may not, nor may Licensee permit any third party, to modify, translate, reverse engineer, decompile,
 * disassemble or create derivative works based on the Licensed Software or any portion thereof. Subject to Section 1.1,
 * Licensee may distribute the Licensed Software to a third party, provided that the recipient agrees to use the Licensed
 * Software on the terms and conditions of this Agreement. Licensee acknowledges that Licensor reserves the right to offer
 * to Licensee or any third party a license for commercial use and distribution of the Licensed Software on terms and
 * conditions different than those contained in this Agreement.
 *
 * 2. OWNERSHIP OF INTELLECTUAL PROPERTY
 *
 * 2.1	Ownership Rights. Except for the limited license rights expressly granted to Licensee under this Agreement, Licensee
 * acknowledges that all right, title and interest in and to the Licensed Software and all intellectual property rights
 * therein shall remain with Licensor or its licensors, as applicable.
 *
 * 3. DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY
 *
 * 3.1 	Disclaimer of Warranty. LICENSOR PROVIDES THE LICENSED SOFTWARE ON A NO-FEE BASIS “AS IS” WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED. LICENSOR EXPRESSLY DISCLAIMS ALL WARRANTIES OR CONDITIONS OF ANY KIND, INCLUDING ANY
 * WARRANTY OF MERCHANTABILITY, TITLE, SECURITY, ACCURACY, NON-INFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * 3,2	Limitation of Liability.  LICENSEE ASSUMES FULL RESPONSIBILITY AND RISK FOR ANY LOSS RESULTING FROM LICENSEE’s
 * DOWNLOADING AND USE OF THE LICENSED SOFTWARE.  IN NO EVENT SHALL LICENSOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, ARISING FROM THE LICENSED SOFTWARE OR LICENSEE’S USE OF
 * THE LICENSED SOFTWARE, REGARDLESS OF WHETHER LICENSOR IS ADVISED, OR HAS OTHER REASON TO KNOW, OR IN FACT KNOWS,
 * OF THE POSSIBILITY OF THE FOREGOING.
 *
 * 3.3	Acknowledgement. Without limiting the generality of Section 3.1, Licensee acknowledges that the Licensed Software
 * is provided as an information resource only, and should not be relied on for any diagnostic or treatment purposes.
 *
 * 4. TERM AND TERMINATION
 *
 * 4.1 	Term. This Agreement commences on the date this Agreement is executed and will continue until terminated in
 * accordance with Section 4.2.
 *
 * 4.2	Termination. If Licensee breaches any provision hereunder, or otherwise engages in any unauthorized use of the
 * Licensed Software, Licensor may terminate this Agreement immediately. Licensee may terminate this Agreement at any
 * time upon written notice to Licensor. Upon termination, the license granted hereunder will terminate and Licensee will
 * immediately cease using the Licensed Software and destroy all copies of the Licensed Software in its possession.
 * Licensee will certify in writing that it has complied with the foregoing obligation.
 *
 * 5. MISCELLANEOUS
 *
 * 5.1	Future Updates. Use of the Licensed Software under this Agreement is subject to the terms and conditions contained
 * herein. New or updated software may require additional or revised terms of use. Licensor will provide notice of and
 * make available to Licensee any such revised terms.
 *
 * 5.2	Entire Agreement. This Agreement, including any Attachments hereto, constitutes the sole and entire agreement
 * between the parties as to the subject matter set forth herein and supersedes are previous license agreements,
 * understandings, or arrangements between the parties relating to such subject matter.
 *
 * 5.2 	Governing Law. This Agreement shall be construed, governed, interpreted and applied in accordance with the
 * internal laws of the State of Maine, U.S.A., without regard to conflict of laws principles. The parties agree that
 * any disputes between them may be heard only in the state or federal courts in the State of Maine, and the parties
 * hereby consent to venue and jurisdiction in those courts.
 *
 * version:6-8-18
 *
 * Daniel Danis, Peter N Robinson, 2020
 */

package org.monarchinitiative.squirls.cli.writers.tabular;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.cli.writers.AnalysisResults;
import org.monarchinitiative.squirls.cli.writers.AnalysisStats;
import org.monarchinitiative.squirls.cli.writers.SettingsData;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;
import org.monarchinitiative.squirls.core.config.FeatureSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class TabularResultWriterTest {

    private static final Path OUTPUT = Path.of("target/test-classes/tabular_output").toAbsolutePath();

    @Autowired
    public VariantsForTesting variantsForTesting;

    @BeforeEach
    public void setUp() throws Exception {
        if (!Files.isDirectory(OUTPUT)) {
            Files.createDirectories(OUTPUT);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void tearDown() {
        if (OUTPUT.toFile().isDirectory()) {
            File[] files = OUTPUT.toFile().listFiles();
            if (files == null) return;
            for (File file : files) {
                file.delete();
            }
            OUTPUT.toFile().delete();
        }
    }

    @Test
    public void write_uncompressedNoFeatures() throws Exception {
        TabularResultWriter writer = new TabularResultWriter("tsv", '\t', false, false, false);

        int nVariantsToReport = 2;
        List<? extends WritableSplicingAllele> variants = List.of(
                variantsForTesting.BRCA2DonorExon15plus2QUID(),
                variantsForTesting.ALPLDonorExon7Minus2(),
                variantsForTesting.VWFAcceptorExon26minus2QUID(),
                variantsForTesting.TSC2AcceptorExon11Minus3());
        AnalysisResults results = AnalysisResults.builder()
                .addAllVariants(variants)
                .analysisStats(AnalysisStats.of(10, 8, 7))
                .settingsData(SettingsData.builder()
                        .nReported(nVariantsToReport)
                        .featureSource(FeatureSource.REFSEQ)
                        .build())
                .build();
        writer.write(results, OUTPUT.resolve("output").toString());

        // the file must exist
        Path expectedOutputFilePath = Paths.get("target/test-classes/tabular_output/output.tsv");
        assertThat(expectedOutputFilePath.toFile().isFile(), equalTo(true));

        // the file content must match the expectations
        List<String> lines;
        try (BufferedReader reader = Files.newBufferedReader(expectedOutputFilePath)) {
            lines = reader.lines().collect(Collectors.toList());
        }

        assertThat(lines, hasSize(nVariantsToReport + 1)); // + header line
        assertThat(lines, hasItem("id\tchrom\tpos\tref\talt\tgene_symbol\ttx_accession\tinterpretation\tsquirls_score"));
        assertThat(lines, hasItem("BRCA2DonorExon15plus2QUID\t13\t32930748\tT\tG\tBRCA2\tNM_000059.3\tpathogenic\t0.95"));
        assertThat(lines, hasItem("ALPLDonorExon7Minus2\t1\t21894739\tA\tG\tALPL\tNM_000478.4\tpathogenic\t0.94"));
    }

    @Test
    public void write_compressedWithSpliceFeatures() throws Exception {
        TabularResultWriter writer = new TabularResultWriter("tsv", '\t', true, false, true);

        int nVariantsToReport = 2;
        List<? extends WritableSplicingAllele> variants = List.of(
                variantsForTesting.BRCA2DonorExon15plus2QUID(),
                variantsForTesting.ALPLDonorExon7Minus2(),
                variantsForTesting.VWFAcceptorExon26minus2QUID(),
                variantsForTesting.TSC2AcceptorExon11Minus3());
        AnalysisResults results = AnalysisResults.builder()
                .addAllVariants(variants)
                .analysisStats(AnalysisStats.of(10, 8, 7))
                .settingsData(SettingsData.builder()
                        .nReported(nVariantsToReport)
                        .featureSource(FeatureSource.REFSEQ)
                        .build())
                .build();
        writer.write(results, OUTPUT.resolve("output").toString());

        // the file must exist
        Path expectedOutputFilePath = Paths.get("target/test-classes/tabular_output/output.tsv.gz");
        assertThat(expectedOutputFilePath.toFile().isFile(), equalTo(true));

        // the file content must match the expectations
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GzipCompressorInputStream(Files.newInputStream(expectedOutputFilePath))))) {
            lines = reader.lines().collect(Collectors.toList());
        }

        assertThat(lines, hasSize(nVariantsToReport + 1)); // + header line
        assertThat(lines, hasItem("id\tchrom\tpos\tref\talt\tgene_symbol\ttx_accession\tinterpretation\tsquirls_score\tsquirls_features"));
        assertThat(lines, hasItem("BRCA2DonorExon15plus2QUID\t13\t32930748\tT\tG\tBRCA2\tNM_000059.3\tpathogenic\t0.95\tNM_000059.3[exon_length=182.0|wt_ri_donor=10.244297856891256|creates_yag_in_agez=0.0|septamer=2.1036|s_strength_diff_acceptor=0.0|ppt_is_truncated=0.0|canonical_donor=9.945443836377912|cryptic_acceptor=-2.5219544938459935|intron_length=41552.0|s_strength_diff_donor=0.0|yag_at_acceptor_minus_three=0.0|alt_ri_best_window_acceptor=6.24199227902568|cryptic_donor=1.3473990820467006|creates_ag_in_agez=0.0|canonical_acceptor=0.0|donor_offset=2.0|phylop=4.010000228881836|alt_ri_best_window_donor=1.6462531025600458|hexamer=1.8216685|acceptor_offset=184.0|wt_ri_acceptor=8.763946772871673]"));
        assertThat(lines, hasItem("ALPLDonorExon7Minus2\t1\t21894739\tA\tG\tALPL\tNM_000478.4\tpathogenic\t0.94\tNM_000478.4[exon_length=144.0|wt_ri_donor=4.867617848006766|creates_yag_in_agez=0.0|septamer=-0.8844000000000001|s_strength_diff_acceptor=0.0|ppt_is_truncated=0.0|canonical_donor=2.447047894181465|cryptic_acceptor=-12.4905210874462|intron_length=2057.0|s_strength_diff_donor=0.0|yag_at_acceptor_minus_three=0.0|alt_ri_best_window_acceptor=-3.06184416990555|cryptic_donor=0.0|creates_ag_in_agez=0.0|canonical_acceptor=0.0|donor_offset=-2.0|phylop=3.5|alt_ri_best_window_donor=2.4205699538253014|hexamer=-1.4957907|acceptor_offset=143.0|wt_ri_acceptor=9.42867691754065]"));
    }

    @Test
    public void write_compressedWithTranscripts() throws Exception {
        TabularResultWriter writer = new TabularResultWriter("tsv", '\t', false, true, false);

        int nVariantsToReport = 2;
        List<? extends WritableSplicingAllele> variants = List.of(
                variantsForTesting.BRCA2DonorExon15plus2QUID(),
                variantsForTesting.ALPLDonorExon7Minus2(),
                variantsForTesting.VWFAcceptorExon26minus2QUID(),
                variantsForTesting.TSC2AcceptorExon11Minus3());
        AnalysisResults results = AnalysisResults.builder()
                .addAllVariants(variants)
                .analysisStats(AnalysisStats.of(10, 8, 7))
                .settingsData(SettingsData.builder()
                        .nReported(nVariantsToReport)
                        .featureSource(FeatureSource.REFSEQ)
                        .build())
                .build();
        writer.write(results, OUTPUT.resolve("output").toString());

        // the file must exist
        Path expectedOutputFilePath = Paths.get("target/test-classes/tabular_output/output.tsv");
        assertThat(expectedOutputFilePath.toFile().isFile(), equalTo(true));

        // the file content must match the expectations
        List<String> lines;
        try (BufferedReader reader = Files.newBufferedReader(expectedOutputFilePath)) {
            lines = reader.lines().collect(Collectors.toList());
        }

        assertThat(lines, hasSize(nVariantsToReport + 1)); // + header line
        assertThat(lines, hasItem("id\tchrom\tpos\tref\talt\tgene_symbol\ttx_accession\tinterpretation\tsquirls_score\ttranscripts"));
        assertThat(lines, hasItem("BRCA2DonorExon15plus2QUID\t13\t32930748\tT\tG\tBRCA2\tNM_000059.3\tpathogenic\t0.95\tNM_000059.3=0.95"));
        assertThat(lines, hasItem("ALPLDonorExon7Minus2\t1\t21894739\tA\tG\tALPL\tNM_000478.4\tpathogenic\t0.94\tNM_000478.4=0.94"));
    }
}