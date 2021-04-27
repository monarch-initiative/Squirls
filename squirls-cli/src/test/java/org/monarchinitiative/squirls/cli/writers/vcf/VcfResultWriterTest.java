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
 * Daniel Danis, Peter N Robinson, 2021
 */

package org.monarchinitiative.squirls.cli.writers.vcf;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFContigHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.cli.writers.AnalysisResults;
import org.monarchinitiative.squirls.cli.writers.AnalysisStats;
import org.monarchinitiative.squirls.cli.writers.SettingsData;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class VcfResultWriterTest {

    private static final Path OUTPUT = Path.of("target/test-classes").toAbsolutePath();

    @Autowired
    private VariantsForTesting variantsForTesting;

    private Path inputPath;

    @BeforeEach
    public void setUp() {
        inputPath = OUTPUT.resolve("toy-input.vcf");
        try (VariantContextWriter writer = new VariantContextWriterBuilder()
                .setOutputPath(inputPath)
                .unsetOption(Options.INDEX_ON_THE_FLY)
                .build()) {
            VCFHeader header = new VCFHeader();
            header.setVCFHeaderVersion(VCFHeaderVersion.VCF4_2);

            header.addMetaDataLine(new VCFContigHeaderLine("##contig=<ID=1,assembly=b37,length=249250621>", header.getVCFHeaderVersion(), "contig", 1));
            header.addMetaDataLine(new VCFContigHeaderLine("##contig=<ID=12,assembly=b37,length=133851895>", header.getVCFHeaderVersion(), "contig", 12));
            writer.writeHeader(header);
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(inputPath);
    }

    @Test
    public void writeCompressed() throws Exception {
        List<? extends WritableSplicingAllele> variants = List.of(
                variantsForTesting.VWFAcceptorExon26minus2QUID(),
                variantsForTesting.ALPLDonorExon7Minus2()
        );
        AnalysisResults results = AnalysisResults.builder()
                .addAllVariants(variants)
                .analysisStats(new AnalysisStats(10, 8, 7))
                .settingsData(SettingsData.builder()
                        .inputPath(inputPath.toString())
                        .nReported(2)
                        .build())
                .build();

        Path output = OUTPUT.resolve("output");

        VcfResultWriter writer = new VcfResultWriter(true);
        writer.write(results, output.toString());

        Path realOutputFile = Path.of(output.toString() + ".vcf.gz");
        assertThat(realOutputFile.toFile().isFile(), equalTo(true));

        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BlockCompressedInputStream(Files.newInputStream(realOutputFile))))) {
            lines = reader.lines().collect(Collectors.toList());
        }

        assertExpectedOutput(lines);
    }

    @Test
    public void writeUncompressed() throws Exception {
        List<? extends WritableSplicingAllele> variants = List.of(
                variantsForTesting.VWFAcceptorExon26minus2QUID(),
                variantsForTesting.ALPLDonorExon7Minus2()
        );
        AnalysisResults results = AnalysisResults.builder()
                .addAllVariants(variants)
                .analysisStats(new AnalysisStats(10, 8, 7))
                .settingsData(SettingsData.builder()
                        .inputPath(inputPath.toString())
                        .nReported(2)
                        .build())
                .build();

        Path output = OUTPUT.resolve("output");

        VcfResultWriter writer = new VcfResultWriter(false);
        writer.write(results, output.toString());

        Path realOutputFile = Path.of(output.toString() + ".vcf");
        assertThat(realOutputFile.toFile().isFile(), equalTo(true));

        List<String> lines;
        try (BufferedReader reader = Files.newBufferedReader(realOutputFile)) {
            lines = reader.lines().collect(Collectors.toList());
        }

        assertExpectedOutput(lines);
    }

    private void assertExpectedOutput(List<String> lines) {
        assertThat(lines, hasSize(8));
        assertThat(lines.get(0), equalTo("##fileformat=VCFv4.2"));
        assertThat(lines.get(1), equalTo("##FILTER=<ID=SQUIRLS,Description=\"Squirls considers the variant as pathogenic if the filter is present\">"));
        assertThat(lines.get(2), equalTo("##INFO=<ID=SQUIRLS_SCORE,Number=A,Type=String,Description=\"Squirls pathogenicity score\">"));
        assertThat(lines.get(3), equalTo("##contig=<ID=1,assembly=b37,length=249250621>"));
        assertThat(lines.get(4), equalTo("##contig=<ID=12,assembly=b37,length=133851895>"));
        assertThat(lines.get(5), equalTo("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO"));
        assertThat(lines.get(6), equalTo("1\t21894739\tALPL_donor_exon7_minus2\tA\tG\t.\tSQUIRLS\tSQUIRLS_SCORE=G|NM_000478.4=0.940000"));
        assertThat(lines.get(7), equalTo("12\t6132066\tVWF_acceptor_2bp_upstream_exon26_quid\tT\tC\t.\tSQUIRLS\tSQUIRLS_SCORE=C|NM_000552.3=0.910000"));
    }
}