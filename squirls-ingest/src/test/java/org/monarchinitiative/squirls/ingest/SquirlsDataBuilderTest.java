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

package org.monarchinitiative.squirls.ingest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.reference.TranscriptModel;
import org.monarchinitiative.squirls.ingest.data.GenomeAssemblyDownloaderTest;
import org.monarchinitiative.variant.api.*;
import org.monarchinitiative.variant.api.impl.DefaultGenomicAssembly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = {TestDataSourceConfig.class})
public class SquirlsDataBuilderTest {

    private static final String ASSEMBLY = "hg19";

    private static final String VERSION = "1910";

    private static final String VERSIONED_ASSEMBLY = VERSION + "_" + ASSEMBLY;

    private static final URL FASTA_URL = GenomeAssemblyDownloaderTest.class.getResource("shortHg19ChromFa.tar.gz");
    private static final URL ASSEMBLY_REPORT_URL = SquirlsDataBuilderTest.class.getResource("GCF_000001405.25_GRCh37.p13_assembly_report.txt");

    private static final List<Contig> CONTIGS = List.of(
            Contig.unknown(),
            Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 100_000, "", "", "chr2"),
            Contig.of(3, "3", SequenceRole.ASSEMBLED_MOLECULE, 200_000, "", "", "chr3"));

    private static final GenomicAssembly GENOMIC_ASSEMBLY = DefaultGenomicAssembly.builder().contigs(CONTIGS).build();

    @Autowired
    public DataSource dataSource;
    private Path buildDir;

    private static List<TranscriptModel> makeTranscripts() {
        Contig chr2 = CONTIGS.get(1);
        TranscriptModel tx = TranscriptModel.coding(chr2, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 10_000, 20_000, 11_000, 19_000,
                "adam", "ADAM",
                List.of(GenomicRegion.zeroBased(chr2, 10_000, 12_000),
                        GenomicRegion.zeroBased(chr2, 14_000, 16_000),
                        GenomicRegion.zeroBased(chr2, 18_000, 20_000)));

        return List.of(tx);
    }

    @BeforeEach
    public void setUp() throws Exception {
        buildDir = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")).resolve("SQUIRLS-TEST"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.deleteFolderAndFiles(buildDir);
    }

    @Test
    public void downloadReferenceGenome() {
        // arrange - nothing to be done

        // act - download a small reference genome
        Runnable rgTask = SquirlsDataBuilder.downloadReferenceGenome(FASTA_URL, buildDir, VERSIONED_ASSEMBLY, true);
        rgTask.run();

        // assert - there should be a FASTA file with index present in the `buildDir`

        assertThat("FASTA file was not generated", buildDir.resolve(String.format("%s.fa", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
        assertThat("FASTA index was not generated", buildDir.resolve(String.format("%s.fa.fai", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
        assertThat("FASTA dictionary was not generated", buildDir.resolve(String.format("%s.fa.dict", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
    }

    @Test
    @Sql("create_schema.sql")
    public void ingestTranscripts() throws Exception {
        // arrange - nothing to be done
        List<TranscriptModel> transcriptModels = makeTranscripts();

        // act
        SquirlsDataBuilder.ingestTranscripts(dataSource, GENOMIC_ASSEMBLY, transcriptModels);

        // assert
        String tmSql = "select CONTIG, BEGIN, END, BEGIN_ON_POS, END_ON_POS, STRAND, TX_ACCESSION " +
                "from SQUIRLS.TRANSCRIPTS";

        List<String> tms = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement tmSt = connection.prepareStatement(tmSql);
             ResultSet tmRs = tmSt.executeQuery()) {
            while (tmRs.next()) {
                tms.add(String.join(",",
                        tmRs.getString("CONTIG"),
                        tmRs.getString("BEGIN"),
                        tmRs.getString("END"),
                        tmRs.getString("BEGIN_ON_POS"),
                        tmRs.getString("END_ON_POS"),
                        tmRs.getString("STRAND"),
                        tmRs.getString("TX_ACCESSION")));
            }
        }
        assertThat(tms, hasSize(1));
        assertThat(tms, hasItem(String.join(",", "2", "10000", "20000", "10000", "20000", "TRUE", "adam")));

        String efSql = "select TX_ID, BEGIN, END, EXON_NUMBER from SQUIRLS.EXONS;";

        List<String> records = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement efSt = connection.prepareStatement(efSql);
             ResultSet efRs = efSt.executeQuery()) {
            while (efRs.next()) {
                records.add(String.join(";",
                        efRs.getString("TX_ID"),
                        efRs.getString("BEGIN"),
                        efRs.getString("END"),
                        efRs.getString("EXON_NUMBER")));
            }
        }
        assertThat(records, hasSize(3));
        assertThat(records, hasItems("1;10000;12000;0", "1;14000;16000;1", "1;18000;20000;2"));
    }

    @Test
    public void buildDatabase() throws Exception {
        URL phylopUrl = SquirlsDataBuilderTest.class.getResource("small.bw");
        Path jannovarDbDir = Paths.get(SquirlsDataBuilderTest.class.getResource("transcripts/hg19").getPath());
        Path yamlPath = Paths.get(SquirlsDataBuilderTest.class.getResource("spliceSites.yaml").getPath());
        Path hexamerPath = Paths.get(SquirlsDataBuilderTest.class.getResource("hexamer-scores.tsv").getPath());
        Path septamerPath = Paths.get(SquirlsDataBuilderTest.class.getResource("septamer-scores.tsv").getPath());

        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa")), is(false));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa.fai")), is(false));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa.dict")), is(false));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.splicing.mv.db")), is(false));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.phylop.bw")), is(false));

        SquirlsDataBuilder.buildDatabase(buildDir, FASTA_URL, ASSEMBLY_REPORT_URL,
                phylopUrl, jannovarDbDir, yamlPath,
                hexamerPath, septamerPath, TestDataSourceConfig.MODEL_PATHS, VERSIONED_ASSEMBLY);

        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa")), is(true));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa.fai")), is(true));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa.dict")), is(true));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.splicing.mv.db")), is(true));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.phylop.bw")), is(true));
    }
}