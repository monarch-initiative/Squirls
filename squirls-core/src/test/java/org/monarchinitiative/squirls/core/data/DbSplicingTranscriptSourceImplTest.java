package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"create_refdict_tables.sql", "insert_refdict_data.sql",
        "create_transcripts_tables.sql", "insert_transcripts_data.sql"})
class DbSplicingTranscriptSourceImplTest {


    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    private DbSplicingTranscriptSource source;

    @BeforeEach
    void setUp() {
        source = new DbSplicingTranscriptSource(dataSource);
    }

    @Test
    void transcriptsExonsAndIntronsAreDecoded() {
        // we expect to get transcripts `FIRST` and `THIRD` by using these coordinates
        List<SplicingTranscript> transcripts = source.fetchTranscripts("chr1", 1000, 5000, referenceDictionary);
        assertThat(transcripts, hasSize(2));

        // 2 transcripts
        transcripts.sort(Comparator.comparing(SplicingTranscript::getAccessionId));

        // ---     FIRST      ---
        SplicingTranscript first = transcripts.get(0);
        assertThat(first.getAccessionId(), is("FIRST"));
        assertThat(first.getChrName(), is("chr1"));
        assertThat(first.getTxBegin(), is(1000));
        assertThat(first.getTxEnd(), is(2000));
        assertThat(first.getStrand(), is(Strand.FWD));

        assertThat(first.getExons(), is(List.of(
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 1200)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1400, 1600)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1800, 2000)).build()
        )));

        assertThat(first.getIntrons(), is(List.of(
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1200, 1400)).setDonorScore(9.433).setAcceptorScore(7.392).build(),
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1600, 1800)).setDonorScore(4.931).setAcceptorScore(7.832).build())
        ));

        // ---     THIRD     ---
        SplicingTranscript third = transcripts.get(1);
        assertThat(third.getAccessionId(), is("THIRD"));
        assertThat(third.getChrName(), is("chr1"));
        assertThat(third.getTxBegin(), is(8_000));
        assertThat(third.getTxEnd(), is(10_000));
        assertThat(third.getStrand(), is(Strand.REV));

        assertThat(third.getExons(), is(List.of(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8000, 8200)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8300, 8500)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8900, 9600)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 9800, 10000)).build())));

        assertThat(third.getIntrons(), is(List.of(
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8200, 8300)).setDonorScore(8.429).setAcceptorScore(4.541).build(),
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8500, 8900)).setDonorScore(5.249).setAcceptorScore(2.946).build(),
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 9600, 9800)).setDonorScore(4.234).setAcceptorScore(1.493).build())));
    }

    @Test
    void fetchTranscriptByAccession() {
        final Optional<SplicingTranscript> txOpt = source.fetchTranscriptByAccession("SECOND", referenceDictionary);
        assertThat(txOpt.isPresent(), is(true));

        final SplicingTranscript tx = txOpt.get();
        assertThat(tx, is(SplicingTranscript.builder()
                .setAccessionId("SECOND")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5000, 6000))
                .addExon(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5000, 5100)).build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5100, 5300))
                        .setDonorScore(5.329)
                        .setAcceptorScore(3.848)
                        .build())
                .addExon(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5300, 5500)).build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5500, 5800))
                        .setDonorScore(9.740)
                        .setAcceptorScore(6.348)
                        .build())
                .addExon(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5800, 5900)).build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5900, 5950))
                        .setDonorScore(5.294)
                        .setAcceptorScore(8.239)
                        .build())
                .addExon(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5950, 6000)).build())
                .build()));
    }

    @Test
    void fetchNonExistingTranscript() {
        final Optional<SplicingTranscript> txOpt = source.fetchTranscriptByAccession("BLABLA", referenceDictionary);
        assertThat(txOpt.isEmpty(), is(true));
    }

    @Test
    void getTranscriptAccessionIds() {
        final List<String> ids = source.getTranscriptAccessionIds();
        assertThat(ids, hasSize(3));
        assertThat(ids, hasItems("FIRST", "SECOND", "THIRD"));
    }
}