package org.monarchinitiative.squirls.ingest.transcripts;

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

import javax.sql.DataSource;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {
        "create_gene_tx_data_table.sql"
})
public class GeneIngestDaoTest {

    @Autowired
    public DataSource dataSource;

    @Autowired
    public ReferenceDictionary rd;

    private GeneIngestDao instance;

    private static RowMapper<String> joiningRowMapper() {
        return (rs, i) -> {
            final ResultSetMetaData md = rs.getMetaData();
            final int nCols = md.getColumnCount();
            return IntStream.range(0, nCols)
                    .mapToObj(j -> {
                        try {
                            return rs.getString(j + 1);
                        } catch (SQLException e) {
                            return "";
                        }
                    })
                    .collect(Collectors.joining(","));
        };
    }

    private static RowMapper<GeneTrack> trackRowMapper() {
        return (rs, i) -> {
            // GENE_ID, CONTIG, BEGIN_POS, END_POS, STRAND, FASTA_SEQUENCE, PHYLOP_VALUES
            return new GeneTrack(
                    rs.getInt(1), // GENE_ID
                    rs.getInt(2), // CONTIG
                    rs.getInt(3), // BEGIN_POS
                    rs.getInt(4), // END_POS
                    rs.getBoolean(5), // STRAND
                    rs.getBytes(6), // FASTA_SEQUENCE
                    rs.getBytes(7) // PHYLOP_VALUES
            );
        };
    }

    @BeforeEach
    public void setUp() {
        instance = new GeneIngestDao(dataSource, rd);
    }

    @Test
    public void insertNull() {
        int i = instance.insertGene(null);
        assertThat(i, is(0));
    }

    @Test
    public void insertEmptyData() {
        final GeneAnnotationData data = new GeneAnnotationData("ABC",
                List.of(),
                new GenomeInterval(rd, Strand.FWD, 2, 10, 14),
                "ACGT",
                new float[]{0f, 0f, 0f, 0f}
        );
        int i = instance.insertGene(data);
        assertThat(i, is(0));
    }

    @Test
    public void insertGene() {
        final GeneAnnotationData data = PojosForTesting.makeGeneAnnotationData(rd);
        final int updated = instance.insertGene(data);

        assertThat(updated, is(12));
        final JdbcTemplate template = new JdbcTemplate(dataSource);

        // check GENE
        final List<String> genes = template.query(
                "select CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, GENE_ID, SYMBOL " +
                        "from SPLICING.GENE",
                joiningRowMapper());
        assertThat(genes.size(), is(1));
        final String gene = genes.get(0);
        assertThat(gene, is("2,99890,99900,100,110,FALSE,0,ALPHA"));

        // check GENE_TRACKS
        final List<GeneTrack> tracks = template.query(
                "select GENE_ID, CONTIG, BEGIN_POS, END_POS, STRAND, FASTA_SEQUENCE, PHYLOP_VALUES " +
                        "from SPLICING.GENE_TRACK",
                trackRowMapper());
        assertThat(tracks.size(), is(1));
        final GeneTrack track = tracks.get(0);
        assertThat(track.gene_id, is(0));
        assertThat(track.contig, is(2));
        assertThat(track.begin, is(99890));
        assertThat(track.end, is(99900));
        assertThat(track.strand, is(false));
        assertThat(track.sequence, is("ACGTacgtAC".getBytes(GeneIngestDao.CHARSET)));
        assertThat(track.phylop, is(new byte[]{63, -128, 0, 0, 64, 0, 0, 0, 64, 64, 0, 0,
                64, -128, 0, 0, 64, -96, 0, 0, 65, 32, 0, 0,
                65, -96, 0, 0, 65, -16, 0, 0, 66, 32, 0, 0,
                66, 72, 0, 0}));

        // check GENE_TO_TX
        final List<String> geneToTx = template.query(
                "select GENE_ID, TX_ID from SPLICING.GENE_TO_TX",
                joiningRowMapper());
        assertThat(geneToTx.size(), is(2));
        assertThat(geneToTx, hasItems("0,0", "0,1"));

        // check TRANSCRIPT
        final List<String> tx = template.query(
                "select TX_ID, CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, ACCESSION_ID " +
                        "from SPLICING.TRANSCRIPT",
                joiningRowMapper());
        assertThat(tx.size(), is(2));
        assertThat(tx, hasItems(
                "0,2,99892,99900,100,108,FALSE,FIRST",
                "1,2,99890,99898,102,110,FALSE,LAST"));

        // check TX_FEATURE_REGION
        final List<String> features = template.query(
                "select TX_ID, CONTIG, BEGIN_POS, END_POS, REGION_TYPE, REGION_NUMBER, PROPERTIES " +
                        "from SPLICING.TX_FEATURE_REGION",
                joiningRowMapper());
        assertThat(features.size(), is(6));
        assertThat(features, hasItems(
                "0,2,99897,99900,ex,0,",
                "0,2,99892,99894,ex,1,",
                "0,2,99894,99897,ir,0,DONOR=0.1;ACCEPTOR=0.2",
                "1,2,99895,99898,ex,0,",
                "1,2,99890,99892,ex,1,",
                "1,2,99892,99895,ir,0,DONOR=0.3;ACCEPTOR=0.4"
        ));
    }

    /**
     * Static class representing a row in the GENE_TRACK table.
     */
    private static class GeneTrack {
        private final int gene_id, contig, begin, end;
        private final boolean strand;
        private final byte[] sequence, phylop;

        public GeneTrack(int gene_id, int contig, int begin, int end, boolean strand, byte[] sequence, byte[] phylop) {
            this.gene_id = gene_id;
            this.contig = contig;
            this.begin = begin;
            this.end = end;
            this.strand = strand;
            this.sequence = sequence;
            this.phylop = phylop;
        }
    }
}