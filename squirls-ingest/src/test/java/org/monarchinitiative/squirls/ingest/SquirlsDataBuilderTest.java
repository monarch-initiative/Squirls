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