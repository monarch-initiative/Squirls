package org.monarchinitiative.squirls.core.data;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.FloatRegion;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {
        "create_gene_tx_data_table.sql",
        "insert_gene_tx_data.sql",
        "create_refdict_tables.sql",
        "insert_refdict_data.sql"
})
public class DbSplicingAnnotationDataSourceTest {

    @Autowired
    public DataSource dataSource;

    @Autowired
    public ReferenceDictionary rd;

    private DbSplicingAnnotationDataSource instance;

    @BeforeEach
    public void setUp() {
        instance = new DbSplicingAnnotationDataSource(dataSource);
    }

    @Test
    public void getAnnotationDataUseCoordinates() {
        String contig = "chr1";
        int begin = 1200;
        int end = 1201;

        final Map<String, SplicingAnnotationData> dataMap = instance.getAnnotationData(contig, begin, end);

        // Expecting to find data for GENE1 - a simulated gene with 1 transcript consisting of 2 exons and 1 intron
        // The tracks contain data for regions 1:1000-1004 - FASTA=ACGT and PHYLOP={1., 2., 3., 4.}
        assertThat(dataMap.size(), is(1));
        assertThat(dataMap.keySet(), hasItem("GENE1"));

        final SplicingAnnotationData data = dataMap.get("GENE1");

        // check tracks
        assertThat(data.getTracks().keySet(), hasItems("phylop", "fasta"));
        final GenomeInterval trackInterval = new GenomeInterval(rd, Strand.FWD, 1, 500, 3500);

        final SequenceRegion fasta = data.getTrack("fasta", SequenceRegion.class);
        assertThat(fasta.getInterval(), is(trackInterval));
        assertThat(fasta.getValue(), is("ACGT".repeat(3000 / 4).getBytes(StandardCharsets.US_ASCII)));

        final FloatRegion phylop = data.getTrack("phylop", FloatRegion.class);

        assertThat(phylop.getInterval(), is(new GenomeInterval(rd, Strand.FWD, 1, 1198, 1203)));
        assertThat(phylop.getValue(), is(new float[]{0.7455148f, 0.8550232f, 0.66827893f, 0.23617701f, 0.69727147f}));

        // check transcripts
        final Collection<SplicingTranscript> txs = data.getTranscripts();
        assertThat(txs.size(), is(1));

        //noinspection OptionalGetWithoutIsPresent checked above
        final SplicingTranscript tx = txs.stream().findFirst().get();
        assertThat(tx.getAccessionId(), is("TX1"));
        assertThat(tx.getChr(), is(1));
        assertThat(tx.getChrName(), is("chr1"));
        assertThat(tx.getTxBegin(), is(1000));
        assertThat(tx.getTxEnd(), is(3000));

        final ImmutableList<SplicingExon> exons = tx.getExons();
        assertThat(exons, hasSize(2));
        assertThat(exons, hasItems(
                SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 1000, 1500))
                        .build(),
                SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 2500, 3000))
                        .build()
        ));

        final ImmutableList<SplicingIntron> introns = tx.getIntrons();
        assertThat(introns, hasSize(1));
        assertThat(introns, hasItem(SplicingIntron.builder()
                .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 1500, 2500))
                .setDonorScore(9.433)
                .setAcceptorScore(7.392)
                .build()));
    }

    @Test
    public void getAnnotationDataUseCoordinatesToGetTxsOnRevStrand() {
        String contig = "chr1";
        int begin = 2000;
        int end = 2001;

        final Map<String, SplicingAnnotationData> dataMap = instance.getAnnotationData(contig, begin, end);

        assertThat(dataMap.keySet(), hasItems("GENE1", "GENE2"));

        final SplicingAnnotationData data = dataMap.get("GENE2");

        // check tracks
        assertThat(data.getTracks().keySet(), hasItems("phylop", "fasta"));
        final GenomeInterval trackInterval = new GenomeInterval(rd, Strand.REV, 1, 5500, 8500).withStrand(Strand.FWD);

        final SequenceRegion fasta = data.getTrack("fasta", SequenceRegion.class);
        assertThat(fasta.getInterval(), is(trackInterval));
        assertThat(fasta.getValue(), is("tcga".repeat(3000 / 4).getBytes(StandardCharsets.US_ASCII)));

        final FloatRegion phylop = data.getTrack("phylop", FloatRegion.class);
        assertThat(phylop.getInterval(), is(new GenomeInterval(rd, Strand.FWD, 1, 1998, 2003).withStrand(Strand.REV)));
        assertThat(phylop.getValue(), is(new float[]{0.8437929f, 0.6994124f, 0.79757345f, 0.1062102f, 0.24914718f}));

        // check transcripts
        final Collection<SplicingTranscript> txs = data.getTranscripts();
        assertThat(txs.size(), is(1));

        //noinspection OptionalGetWithoutIsPresent checked above
        final SplicingTranscript tx2 = txs.stream().filter(tx -> tx.getAccessionId().equals("TX2")).findFirst().get();
        assertThat(tx2.getAccessionId(), is("TX2"));
        assertThat(tx2.getChr(), is(1));
        assertThat(tx2.getChrName(), is("chr1"));
        assertThat(tx2.getTxBegin(), is(6000));
        assertThat(tx2.getTxEnd(), is(8000));
        assertThat(tx2.getStrand(), is(Strand.REV));

        final ImmutableList<SplicingExon> exons = tx2.getExons();
        assertThat(exons, hasSize(1));
        assertThat(exons, hasItem(
                SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.REV, 1, 6000, 8000))
                        .build()
        ));

        assertThat(tx2.getIntrons(), is(empty()));
    }


    @Test
    @Disabled
    public void generateFloatsAsHexadecimalStrings() {
        final List<Float> floats = new Random(456).doubles(3000)
                .boxed()
                .map(Double::floatValue)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (Float f : floats) {
            final byte[] array = ByteBuffer.allocate(4).putFloat(f).array();
            for (byte b : array) {
                sb.append(String.format("%02X", b));
            }
        }

        System.err.println(sb.toString());
    }
}