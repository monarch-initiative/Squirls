package org.monarchinitiative.squirls.ingest.reference;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.ingest.PojosForTesting;
import org.monarchinitiative.squirls.ingest.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.jdbc.Sql;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import javax.sql.DataSource;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"create_ref_sequence_table.sql"})
public class ReferenceSequenceIngestDaoTest {

    @Autowired
    public DataSource dataSource;

    @Autowired
    public ReferenceDictionary rd;

    private ReferenceSequenceIngestDao dao;

    private static RowMapper<GeneSequence> rowMapper(ReferenceDictionary rd) {
        return (rs, i) -> new GeneSequence(rs.getString(1),
                SequenceInterval.builder()
                        .interval(new GenomeInterval(rd,
                                rs.getBoolean(5) ? Strand.FWD : Strand.REV,
                                rs.getInt(2), rs.getInt(3), rs.getInt(4)))
                        .sequence(new String(rs.getBytes(6), ReferenceSequenceIngestDao.CHARSET))
                        .build());
    }

    @BeforeEach
    public void setUp() {
        dao = new ReferenceSequenceIngestDao(dataSource);
    }

    @Test
    public void insertSequence() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        List<GeneSequence> records = template.query("SELECT * FROM SPLICING.REF_SEQUENCE", rowMapper(rd));
        // the table is empty at the beginning
        assertThat(records, hasSize(0));

        SequenceInterval interval = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(rd);
        int updated = dao.insertSequence("BLABLA", interval);

        // inserting the item updates 1 row
        assertThat(updated, is(1));

        records = template.query("SELECT SYMBOL, CONTIG, BEGIN_POS, END_POS, STRAND, FASTA_SEQUENCE " +
                " FROM SPLICING.REF_SEQUENCE", rowMapper(rd));

        // now the table contains an item
        assertThat(records, hasSize(1));

        // and the item meets the expectations
        final GeneSequence seq = records.get(0);
        assertThat(seq.symbol, is("BLABLA"));
        assertThat(seq.interval, is(interval));
    }

    private static class GeneSequence {
        private final String symbol;
        private final SequenceInterval interval;

        private GeneSequence(String symbol, SequenceInterval interval) {
            this.symbol = symbol;
            this.interval = interval;
        }
    }
}