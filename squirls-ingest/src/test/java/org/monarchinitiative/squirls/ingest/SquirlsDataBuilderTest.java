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

import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.squirls.ingest.data.GenomeAssemblyDownloaderTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;

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
@Sql(scripts = {"dao/create_pwm_tables.sql", "dao/create_transcript_intron_exon_tables.sql"})
public class SquirlsDataBuilderTest {

    private static final String ASSEMBLY = "hg19";

    private static final String VERSION = "1910";

    private static final String VERSIONED_ASSEMBLY = VERSION + "_" + ASSEMBLY;

    private static final URL FASTA_URL = GenomeAssemblyDownloaderTest.class.getResource("shortHg19ChromFa.tar.gz");

    private Path buildDir;

    @Autowired
    private GenomeSequenceAccessor accessor;

    @Autowired
    private SplicingInformationContentCalculator splicingInformationContentCalculator;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private List<TranscriptModel> transcriptModels;

    @BeforeEach
    void setUp() throws Exception {
        buildDir = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")).resolve("3S-TEST"));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.deleteFolderAndFiles(buildDir);
    }

    @Test
    void downloadReferenceGenome() {
        // arrange - nothing to be done

        // act - download a small reference genome
        final Runnable rgTask = SquirlsDataBuilder.downloadReferenceGenome(FASTA_URL, buildDir, VERSIONED_ASSEMBLY, true);
        rgTask.run();

        // assert - there should be a FASTA file with index present in the `buildDir`

        assertThat("FASTA file was not generated", buildDir.resolve(String.format("%s.fa", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
        assertThat("FASTA index was not generated", buildDir.resolve(String.format("%s.fa.fai", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
        assertThat("FASTA dictionary was not generated", buildDir.resolve(String.format("%s.fa.dict", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
    }

    @Test
    void ingestTranscripts() throws Exception {
        // arrange - nothing to be done

        // act
        SquirlsDataBuilder.ingestTranscripts(dataSource, accessor.getReferenceDictionary(), accessor, transcriptModels, splicingInformationContentCalculator);

        // assert
        String tmSql = "select CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, TX_ACCESSION " +
                "from SPLICING.TRANSCRIPTS";

        List<String> tms = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement tmSt = connection.prepareStatement(tmSql);
             ResultSet tmRs = tmSt.executeQuery()) {
            while (tmRs.next()) {
                tms.add(String.join(",",
                        tmRs.getString("CONTIG"),
                        tmRs.getString("BEGIN_POS"),
                        tmRs.getString("END_POS"),
                        tmRs.getString("BEGIN_ON_FWD"),
                        tmRs.getString("END_ON_FWD"),
                        tmRs.getString("STRAND"),
                        tmRs.getString("TX_ACCESSION")));
            }
        }
        assertThat(tms, hasSize(1));
        assertThat(tms, hasItem(String.join(",", "0", "10000", "20000", "10000", "20000", "TRUE", "adam")));

        String efSql = "select CONTIG, BEGIN_POS, END_POS, TX_ACCESSION, REGION_TYPE, PROPERTIES, REGION_NUMBER from SPLICING.FEATURE_REGIONS;";

        List<String> records = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement efSt = connection.prepareStatement(efSql);
             ResultSet efRs = efSt.executeQuery()) {
            while (efRs.next()) {
                records.add(String.join(";",
                        efRs.getString("CONTIG"),
                        efRs.getString("BEGIN_POS"),
                        efRs.getString("END_POS"),
                        efRs.getString("TX_ACCESSION"),
                        efRs.getString("REGION_TYPE"),
                        efRs.getString("PROPERTIES"),
                        efRs.getString("REGION_NUMBER")));
            }
        }
        assertThat(records, hasSize(5));
        assertThat(records, hasItems(
                "0;10000;12000;adam;ex;;0",
                "0;14000;16000;adam;ex;;1",
                "0;18000;20000;adam;ex;;2",
                "0;12000;14000;adam;ir;DONOR=-5.641756189392563;ACCEPTOR=-22.149718912705787;0",
                "0;16000;18000;adam;ir;DONOR=-4.676134711788632;ACCEPTOR=-14.459319682085656;1"));
    }

    @Test
    public void buildDatabase() throws Exception {
        final URL phylopUrl = SquirlsDataBuilderTest.class.getResource("small.bw");
        final Path jannovarDbDir = Paths.get(SquirlsDataBuilderTest.class.getResource("transcripts/hg19").getPath());
        final Path yamlPath = Paths.get(SquirlsDataBuilderTest.class.getResource("spliceSites.yaml").getPath());
        final Path hexamerPath = Paths.get(SquirlsDataBuilderTest.class.getResource("hexamer-scores.tsv").getPath());
        final Path septamerPath = Paths.get(SquirlsDataBuilderTest.class.getResource("septamer-scores.tsv").getPath());

        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa")), is(false));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa.fai")), is(false));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa.dict")), is(false));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.splicing.mv.db")), is(false));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.phylop.bw")), is(false));

        SquirlsDataBuilder.buildDatabase(buildDir, FASTA_URL, phylopUrl, jannovarDbDir, yamlPath, hexamerPath, septamerPath, TestDataSourceConfig.MODEL_PATHS, VERSIONED_ASSEMBLY);

        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa")), is(true));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa.fai")), is(true));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.fa.dict")), is(true));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.splicing.mv.db")), is(true));
        assertThat(Files.isRegularFile(buildDir.resolve("1910_hg19.phylop.bw")), is(true));
    }
}