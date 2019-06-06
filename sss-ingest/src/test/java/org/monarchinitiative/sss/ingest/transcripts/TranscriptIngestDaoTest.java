package org.monarchinitiative.sss.ingest.transcripts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.ingest.TestDataInstances;
import org.monarchinitiative.sss.ingest.TestDataSourceConfig;
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
@Sql(scripts = {"file:src/test/resources/sql/create_schema.sql",
        "file:src/test/resources/sql/create_transcript_intron_exon_tables.sql"})
class TranscriptIngestDaoTest {

    @Autowired
    private DataSource dataSource;

    private TranscriptIngestDao instance;

    private static void printAll(DataSource dataSource) throws Exception {
        String transcriptsSql = "SELECT * FROM SPLICING.TRANSCRIPTS";
        String exonsSql = "SELECT * FROM SPLICING.EXONS";
        String intronsSql = "SELECT * FROM SPLICING.INTRONS";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement transcripts = connection.prepareStatement(transcriptsSql);
                 PreparedStatement exons = connection.prepareStatement(exonsSql);
                 PreparedStatement introns = connection.prepareStatement(intronsSql)) {
                ResultSet trs = transcripts.executeQuery();
                System.out.println("=================   TRANSCRIPTS   =================");
                while (trs.next()) {
                    System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s",
                            trs.getString(1), trs.getString(2), trs.getString(3),
                            trs.getString(4), trs.getString(5), trs.getString(6),
                            trs.getString(7)));
                }

                System.out.println("=================      EXONS      =================");
                ResultSet ers = exons.executeQuery();
                while (ers.next()) {
                    System.out.println(String.format("%s\t%s\t%s",
                            ers.getString(1), ers.getString(2), ers.getString(3)));
                }

                System.out.println("=================      INTRONS    =================");
                ResultSet irs = introns.executeQuery();
                while (irs.next()) {
                    System.out.println(String.format("%s\t%s\t%s\t%s\t%s",
                            irs.getString(1), irs.getString(2), irs.getString(3),
                            irs.getString(4), irs.getString(5)));
                }
            }
        }
    }

    private static RowMapper<String> rowMapper() {
        return (rs, i) -> rs.getString(1);
    }

    @BeforeEach
    void setUp() {
        instance = new TranscriptIngestDao(dataSource);
    }

    @Test
    void insertCorrectData() throws Exception {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        List<String> records = template.query("SELECT * FROM SPLICING.TRANSCRIPTS", rowMapper());
        assertThat(records, hasSize(0));

        SplicingTranscript alpha = TestDataInstances.makeAlphaTranscript();
        int inserted = instance.insertTranscript(alpha);
        SplicingTranscript beta = TestDataInstances.makeBetaTranscript();
        inserted += instance.insertTranscript(beta);

        records = template.query("SELECT * FROM SPLICING.TRANSCRIPTS", rowMapper());
        assertThat(records, hasSize(2));

        records = template.query("SELECT * FROM SPLICING.EXONS", rowMapper());
        assertThat(records, hasSize(4));

        records = template.query("SELECT * FROM SPLICING.INTRONS", rowMapper());
        assertThat(records, hasSize(2));

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