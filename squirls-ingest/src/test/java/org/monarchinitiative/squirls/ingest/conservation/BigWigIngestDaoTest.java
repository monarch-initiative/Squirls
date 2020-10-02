package org.monarchinitiative.squirls.ingest.conservation;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.ingest.TestDataSourceConfig;
import org.monarchinitiative.squirls.ingest.reference.ReferenceSequenceIngestDao;
import org.monarchinitiative.squirls.ingest.reference.ReferenceSequenceIngestDaoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.jdbc.Sql;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import javax.sql.DataSource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"create_ref_sequence_table.sql"})
public class BigWigIngestDaoTest {

    @Autowired
    public DataSource dataSource;

    @Autowired
    public ReferenceDictionary rd;

    private BigWigIngestDao dao;

    private static RowMapper<BigWigStuff> rowMapper() {
        return (rs, i) -> new BigWigStuff(
                rs.getString(1), // symbol
                rs.getInt(2), // contig
                rs.getInt(3), // begin
                rs.getInt(4), // end
                rs.getBoolean(5), // strand
                rs.getBytes(6) // values
        );
    }

    @BeforeEach
    public void setUp() {
        dao = new BigWigIngestDao(dataSource);
    }

    @Test
    public void insertScores() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        List<BigWigStuff> records = template.query("SELECT * FROM SPLICING.PHYLOP_SCORE", rowMapper());

        // the table is empty at the beginning
        assertThat(records, hasSize(0));

        String symbol = "ABCD94";
        GenomeInterval interval = new GenomeInterval(rd, Strand.FWD, 94, 10, 15);
        List<Float> scores = List.of(0f, 2f, Float.MIN_VALUE, Float.MAX_VALUE, Float.NaN);

        final int updated = dao.insertScores(symbol, interval, scores);

        // inserting the item updates 1 row
        assertThat(updated, is(1));

        records = template.query("SELECT SYMBOL, CONTIG, BEGIN_POS, END_POS, STRAND, PHYLOP_VALUES " +
                " FROM SPLICING.PHYLOP_SCORE", rowMapper());

        // now the table contains an item
        assertThat(records, hasSize(1));

        // and the item meets the expectations
        final BigWigStuff data = records.get(0);
        assertThat(data.symbol, is(symbol));
        assertThat(data.contig, is(94));
        assertThat(data.begin, is(10));
        assertThat(data.end, is(15));
        assertThat(data.strand, is(true));
        assertThat(data.values, is(new byte[]{
                0, 0, 0, 0,       // 0.f
                64, 0, 0, 0,      // 2.f
                0, 0, 0, 1,       // MIN
                127, 127, -1, -1, // MAX
                127, -64, 0, 0    // NaN
        }));
    }

    private static class BigWigStuff {

        private final String symbol;
        private final int contig;
        private final int begin;
        private final int end;
        private final boolean strand;
        private final byte[] values;

        private BigWigStuff(String symbol, int contig, int begin, int end, boolean strand, byte[] values) {
            this.symbol = symbol;
            this.contig = contig;
            this.begin = begin;
            this.end = end;
            this.strand = strand;
            this.values = values;
        }
    }
}