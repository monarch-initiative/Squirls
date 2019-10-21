package org.monarchinitiative.threes.ingest;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.ThreeSException;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"file:src/test/resources/sql/create_schema.sql",
        "file:src/test/resources/sql/create_pwm_tables.sql",
        "file:src/test/resources/sql/create_contig_tables.sql",
        "file:src/test/resources/sql/create_transcript_intron_exon_tables.sql"})
class ThreesDataBuilderTest {

    private static final String TRANSCRIPT_SOURCE = "refseq";

    private static final String ASSEMBLY = "hg19";

    private static final String VERSION = "1910";

    private static final URL FASTA_URL = ThreesDataBuilderTest.class.getResource("shortHg19ChromFa.tar.gz");

    private Path buildDir;

    @Autowired
    private GenomeSequenceAccessor accessor;

    @Autowired
    private SplicingInformationContentCalculator splicingInformationContentCalculator;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Map<String, Integer> contigLengthMap;

    @Autowired
    private List<TranscriptModel> transcriptModels;

    @Autowired
    private JannovarData jannovarData;

    @Autowired
    private SplicingPwmData splicingPwmData;


    private static List<String> parseResultSet(ResultSet rs, Function<ResultSet, String> mapper) throws SQLException {
        List<String> results = new ArrayList<>();

        while (rs.next()) {
            results.add(mapper.apply(rs));
        }

        return results;
    }

    @BeforeEach
    void setUp() throws Exception {
        buildDir = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")).resolve("3S-TEST"));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.deleteFolderAndFiles(buildDir);
    }

    /**
     * Test to insert small PWMs representing splice donor and acceptor sites. Test that metadata is inserted as well.
     *
     * @throws Exception bla
     */
    @Test
    void processPwms() throws Exception {
        // arrange - nothing to be done here

        // act - insert small PWMs into db
        SplicingPwmData data = SplicingPwmData.builder()
                .setDonor(PojosForTesting.makeFakeDonorMatrix())
                .setAcceptor(PojosForTesting.makeFakeAcceptorMatrix())
                .setParameters(PojosForTesting.makeFakeSplicingParameters())
                .build();
        ThreesDataBuilder.processPwms(dataSource, data);

        // assert - get the data from expected location
        DoubleMatrix actualDonor = new DoubleMatrix(4, 5);
        DoubleMatrix actualAcceptor = new DoubleMatrix(4, 9);
        String pwmDataSql = "select PWM_NAME, ROW_IDX, COL_IDX, CELL_VALUE from SPLICING.PWM_DATA";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement dataStatement = connection.prepareStatement(pwmDataSql)) {
            ResultSet rs = dataStatement.executeQuery();


            while (rs.next()) {
                final String name = rs.getString("PWM_NAME");
                final int row = rs.getInt("ROW_IDX");
                final int col = rs.getInt("COL_IDX");
                final double value = rs.getDouble("CELL_VALUE");
                switch (name) {
                    case ThreesDataBuilder.DONOR_NAME:
                        actualDonor.put(row, col, value);
                        break;
                    case ThreesDataBuilder.ACCEPTOR_NAME:
                        actualAcceptor.put(row, col, value);
                        break;
                    default:
                        throw new Exception(String.format("Unexpected PWM name '%s'", name));
                }
            }
        }
        assertThat(PojosForTesting.makeFakeDonorMatrix(), is(equalTo(actualDonor)));
        assertThat(PojosForTesting.makeFakeAcceptorMatrix(), is(equalTo(actualAcceptor)));

        String pwmMetadataSql = "select PWM_NAME, PWM_KEY, PWM_VALUE from SPLICING.PWM_METADATA";
        List<String> lines = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement metadataStatement = connection.prepareStatement(pwmMetadataSql)) {
            final ResultSet rs = metadataStatement.executeQuery();
            // inserted content is formatted into lines here
            lines.addAll(parseResultSet(rs, res -> {
                try {
                    final String name = res.getString("PWM_NAME");
                    final String key = res.getString("PWM_KEY");
                    final String value = res.getString("PWM_VALUE");
                    return String.join("\t", name, key, value);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));

        }
        assertThat(lines, hasSize(4));
        assertThat(lines, hasItems(
                String.join("\t", ThreesDataBuilder.DONOR_NAME, "EXON", "2"),
                String.join("\t", ThreesDataBuilder.DONOR_NAME, "INTRON", "3"),
                String.join("\t", ThreesDataBuilder.ACCEPTOR_NAME, "EXON", "4"),
                String.join("\t", ThreesDataBuilder.ACCEPTOR_NAME, "INTRON", "5")
        ));
    }

    @Test
    void processTranscripts() throws Exception {
        // arrange - nothing to be done

        // act
        ThreesDataBuilder.processTranscripts(dataSource, accessor, contigLengthMap, transcriptModels, splicingInformationContentCalculator);

        // assert
        String tmSql = "select CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, TX_ACCESSION " +
                "from SPLICING.TRANSCRIPTS";

        List<String> tms = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement tmSt = connection.prepareStatement(tmSql)) {
            ResultSet tmRs = tmSt.executeQuery();
            tms.addAll(parseResultSet(tmRs, rs -> {
                try {
                    return String.join("\t",
                            tmRs.getString("CONTIG"),
                            tmRs.getString("BEGIN_POS"),
                            tmRs.getString("END_POS"),
                            tmRs.getString("BEGIN_ON_FWD"),
                            tmRs.getString("END_ON_FWD"),
                            tmRs.getString("STRAND"),
                            tmRs.getString("TX_ACCESSION"));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        assertThat(tms, hasSize(1));
        assertThat(tms, hasItem(String.join("\t", "chr2", "10000", "20000", "10000", "20000", "TRUE", "adam")));

        String exonsSql = "select TX_ACCESSION as tx, BEGIN_POS as bp, END_POS as ep from SPLICING.EXONS";
        String intronsSql = "select TX_ACCESSION as tx, BEGIN_POS as bp, END_POS as ep, " +
                "DONOR_SCORE as ds, ACCEPTOR_SCORE as ass from SPLICING.INTRONS";

        List<String> exons = new ArrayList<>();
        List<String> introns = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement exonsPs = connection.prepareStatement(exonsSql);
             PreparedStatement intronsPs = connection.prepareStatement(intronsSql)) {

            exons.addAll(parseResultSet(exonsPs.executeQuery(), rs -> {
                try {
                    return String.join("\t",
                            rs.getString("tx"),
                            rs.getString("bp"),
                            rs.getString("ep"));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));

            introns.addAll(parseResultSet(intronsPs.executeQuery(), rs -> {
                try {
                    return String.join("\t",
                            rs.getString("tx"),
                            rs.getString("bp"),
                            rs.getString("ep"),
                            String.format("%.5f", rs.getDouble("ds")),
                            String.format("%.5f", rs.getDouble("ass")));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        assertThat(exons, hasSize(3));
        assertThat(exons, hasItems(
                String.join("\t", "adam", "10000", "12000"),
                String.join("\t", "adam", "14000", "16000"),
                String.join("\t", "adam", "18000", "20000")
        ));

        assertThat(introns, hasSize(2));
        assertThat(introns, hasItems(
                String.join("\t", "adam", "12000", "14000", "-5.64176", "-22.14972"),
                String.join("\t", "adam", "16000", "18000", "-4.67613", "-14.45932")
        ));
    }

    @Test
    void downloadReferenceGenome() throws ThreeSException {
        // arrange - nothing to be done

        // act - download a small reference genome
        ThreesDataBuilder.downloadReferenceGenome(FASTA_URL, buildDir, ASSEMBLY, VERSION, true);

        // assert - there should be a FASTA file with index present in the `buildDir`
        String versionedAssembly = VERSION + "_" + ASSEMBLY;

        assertThat("FASTA file was not generated", buildDir.resolve(String.format("%s.fa", versionedAssembly)).toFile().isFile(), is(true));
        assertThat("FASTA index was not generated", buildDir.resolve(String.format("%s.fa.fai", versionedAssembly)).toFile().isFile(), is(true));
    }

    @Test
    void processContigs() throws Exception {
        // arrange - nothing to be done

        // act
        ThreesDataBuilder.processContigs(dataSource, contigLengthMap);

        // assert
        List<String> results = new ArrayList<>();
        String contigSql = "select CONTIG, CONTIG_LENGTH from SPLICING.CONTIGS";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement contigPs = connection.prepareStatement(contigSql)) {
            results.addAll(parseResultSet(contigPs.executeQuery(), rs -> {
                try {
                    return String.join("\t",
                            rs.getString("CONTIG"),
                            rs.getString("CONTIG_LENGTH"));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        assertThat(results, hasSize(2));
        assertThat(results, hasItems(
                String.join("\t", "chr2", "100000"),
                String.join("\t", "chr3", "200000")
        ));
    }

    @Test
    void buildThreesDatabase() throws Exception {
        // arrange - nothing to be done

        // act
        ThreesDataBuilder.buildThreesDatabase(jannovarData, TRANSCRIPT_SOURCE, accessor, splicingPwmData, buildDir, ASSEMBLY, VERSION);

        // assert
        String versionedAssembly = VERSION + "_" + ASSEMBLY;
        // there should be database file present in the `buildDir`
        assertThat("Database file was not generated", buildDir.resolve(String.format("%s_splicing_%s.mv.db", versionedAssembly, TRANSCRIPT_SOURCE)).toFile().isFile(), is(true));
    }
}