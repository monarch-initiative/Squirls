package org.monarchinitiative.squirls.ingest;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.SquirlsException;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.squirls.ingest.conservation.BigWigAccessor;
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
@Sql(scripts = {
        "pwm/create_pwm_tables.sql",
        "transcripts/create_gene_tx_data_table.sql"
})
public class SquirlsDataBuilderTest {

    private static final String ASSEMBLY = "hg19";

    private static final String VERSION = "1910";

    private static final String VERSIONED_ASSEMBLY = VERSION + "_" + ASSEMBLY;

    private static final URL FASTA_URL = SquirlsDataBuilderTest.class.getResource("shortHg19ChromFa.tar.gz");

    private Path buildDir;

    @Autowired
    public GenomeSequenceAccessor accessor;

    @Autowired
    public SplicingInformationContentCalculator splicingInformationContentCalculator;

    @Autowired
    public DataSource dataSource;

    @Autowired
    public List<TranscriptModel> transcriptModels;

    @Autowired
    public ReferenceDictionary referenceDictionary;

    @Autowired
    public BigWigAccessor bigWigAccessor;

    @BeforeEach
    public void setUp() throws Exception {
        buildDir = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")).resolve("3S-TEST"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.deleteFolderAndFiles(buildDir);
    }

    @Test
    public void downloadReferenceGenome() throws SquirlsException {
        // arrange - nothing to be done

        // act - download a small reference genome
        SquirlsDataBuilder.downloadReferenceGenome(FASTA_URL, buildDir, VERSIONED_ASSEMBLY, true);

        // assert - there should be a FASTA file with index present in the `buildDir`

        assertThat("FASTA file was not generated", buildDir.resolve(String.format("%s.fa", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
        assertThat("FASTA index was not generated", buildDir.resolve(String.format("%s.fa.fai", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
        assertThat("FASTA dictionary was not generated", buildDir.resolve(String.format("%s.fa.dict", VERSIONED_ASSEMBLY)).toFile().isFile(), is(true));
    }

    @Test
    public void ingestTranscripts() throws Exception {
        // arrange - nothing to be done

        // act
        SquirlsDataBuilder.ingestTranscripts(dataSource, referenceDictionary, accessor, bigWigAccessor, splicingInformationContentCalculator, transcriptModels);

        // assert
        String tmSql = "select TX_ID, CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, ACCESSION_ID " +
                "from SPLICING.TRANSCRIPT";

        List<String> tms = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement tmSt = connection.prepareStatement(tmSql);
             ResultSet tmRs = tmSt.executeQuery()) {
            while (tmRs.next()) {
                tms.add(String.join(",",
                        tmRs.getString("TX_ID"),
                        tmRs.getString("CONTIG"),
                        tmRs.getString("BEGIN_POS"),
                        tmRs.getString("END_POS"),
                        tmRs.getString("BEGIN_ON_FWD"),
                        tmRs.getString("END_ON_FWD"),
                        tmRs.getString("STRAND"),
                        tmRs.getString("ACCESSION_ID")));
            }
        }
        assertThat(tms, hasSize(1));
        assertThat(tms, hasItem(String.join(",", "0", "2", "10000", "20000", "10000", "20000", "TRUE", "adam")));



        String efSql = "select TX_ID, CONTIG, BEGIN_POS, END_POS, REGION_TYPE, PROPERTIES, REGION_NUMBER " +
                "from SPLICING.TX_FEATURE_REGION;";
        List<String> records = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement efSt = connection.prepareStatement(efSql);
             ResultSet efRs = efSt.executeQuery()) {
            while (efRs.next()) {
                records.add(String.join(";",
                        efRs.getString("TX_ID"),
                        efRs.getString("CONTIG"),
                        efRs.getString("BEGIN_POS"),
                        efRs.getString("END_POS"),
                        efRs.getString("REGION_TYPE"),
                        efRs.getString("PROPERTIES"),
                        efRs.getString("REGION_NUMBER")));
            }
        }
        assertThat(records, hasSize(5));
        assertThat(records, hasItems(
                "0;2;10000;12000;ex;;0",
                "0;2;14000;16000;ex;;1",
                "0;2;18000;20000;ex;;2",
                "0;2;12000;14000;ir;DONOR=-5.641756189392563;ACCEPTOR=-22.149718912705787;0",
                "0;2;16000;18000;ir;DONOR=-4.676134711788632;ACCEPTOR=-14.459319682085656;1"));



        String geneSql = "select CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, GENE_ID, SYMBOL " +
                "from SPLICING.GENE";
        List<String> genes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement geneSt = connection.prepareStatement(geneSql);
             ResultSet rs = geneSt.executeQuery()) {
            while (rs.next()) {
                genes.add(String.join(";",
                        rs.getString("CONTIG"),
                        rs.getString("BEGIN_POS"),
                        rs.getString("END_POS"),
                        rs.getString("BEGIN_ON_FWD"),
                        rs.getString("END_ON_FWD"),
                        rs.getString("STRAND"),
                        rs.getString("GENE_ID"),
                        rs.getString("SYMBOL")));
            }
        }
        assertThat(genes, hasSize(1));
        assertThat(genes, hasItem("2;10000;20000;10000;20000;TRUE;0;ADAM"));



        String geneTrackSql = "select GENE_ID, CONTIG, BEGIN_POS, END_POS, STRAND " +
                "from SPLICING.GENE_TRACK";
        List<String> geneTracks = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement gtPs = connection.prepareStatement(geneTrackSql);
             ResultSet rs = gtPs.executeQuery()) {
            while (rs.next()) {
                geneTracks.add(String.join(";",
                        rs.getString("GENE_ID"),
                        rs.getString("CONTIG"),
                        rs.getString("BEGIN_POS"),
                        rs.getString("END_POS"),
                        rs.getString("STRAND")));
            }
        }
        assertThat(geneTracks, hasSize(1));
        assertThat(geneTracks, hasItem("0;2;9500;20500;TRUE"));



        String geneToTxSql = "select GENE_ID, TX_ID " +
                "from SPLICING.GENE_TO_TX";
        List<String> geneToTx = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(geneToTxSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                geneToTx.add(String.join(";",
                        rs.getString("GENE_ID"),
                        rs.getString("TX_ID")));
            }
        }
        assertThat(geneToTx, hasSize(1));
        assertThat(geneToTx, hasItem("0;0"));
    }
}