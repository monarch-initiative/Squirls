package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.ingest.PojosForTesting;
import org.monarchinitiative.squirls.ingest.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"create_transcript_intron_exon_tables.sql"})
class TranscriptIngestDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    private TranscriptIngestDao instance;

    private static void printAll(DataSource dataSource) throws Exception {
        String transcriptsSql = "SELECT * FROM SPLICING.TRANSCRIPTS";
        String exonsSql = "SELECT * FROM SPLICING.FEATURE_REGIONS";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement transcripts = connection.prepareStatement(transcriptsSql);
                 PreparedStatement exons = connection.prepareStatement(exonsSql)) {
                ResultSet trs = transcripts.executeQuery();
                System.out.println("=================   TRANSCRIPTS   =================");
                while (trs.next()) {
                    System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s",
                            trs.getString(1), trs.getString(2), trs.getString(3),
                            trs.getString(4), trs.getString(5), trs.getString(6),
                            trs.getString(7)));
                }

                System.out.println("=================      RECORDS      =================");
                ResultSet ers = exons.executeQuery();
                while (ers.next()) {
                    System.out.println(String.format("%s\t%s\t%s",
                            ers.getString(1), ers.getString(2), ers.getString(3)));
                }
            }
        }
    }

    private static RowMapper<String> rowMapper() {
        return (rs, i) -> rs.getString(1);
    }

    @BeforeEach
    void setUp() {
        instance = new TranscriptIngestDao(dataSource, referenceDictionary);
    }

    @Test
    void insertCorrectData() throws Exception {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        List<String> records = template.query("SELECT * FROM SPLICING.TRANSCRIPTS", rowMapper());
        assertThat(records, hasSize(0));

        SplicingTranscript alpha = PojosForTesting.makeAlphaTranscript(referenceDictionary);
        int inserted = instance.insertTranscript(alpha);
        SplicingTranscript beta = PojosForTesting.makeBetaTranscript(referenceDictionary);
        inserted += instance.insertTranscript(beta);

        records = template.query("SELECT * FROM SPLICING.TRANSCRIPTS", rowMapper());
        assertThat(records, hasSize(2));

        records = template.query("SELECT * FROM SPLICING.FEATURE_REGIONS", rowMapper());
        assertThat(records, hasSize(6));

        assertThat(inserted, is(2));
    }

    @Test
    void insertNull() {
        int i = instance.insertTranscript(null);
        assertThat(i, is(0));
    }

    @Test
    void insertEmptyData() {
        int i = instance.insertTranscript(SplicingTranscript.getDefaultInstance());
        assertThat(i, is(0));
    }
}