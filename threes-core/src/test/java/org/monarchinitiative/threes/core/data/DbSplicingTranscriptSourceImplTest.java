package org.monarchinitiative.threes.core.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.model.SplicingExon;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"file:src/test/resources/sql/create_transcripts_tables.sql",
        "file:src/test/resources/sql/insert_transcripts_data.sql"})
class DbSplicingTranscriptSourceImplTest {


    @Autowired
    private DataSource dataSource;

    private DbSplicingTranscriptSource source;

    @BeforeEach
    void setUp() {
        source = new DbSplicingTranscriptSource(dataSource);
    }

    @Test
    void transcriptsExonsAndIntronsAreDecoded() {
        List<SplicingTranscript> transcripts = source.fetchTranscripts("chr1", 500, 7000);
        assertThat(transcripts, hasSize(2));

        // 2 transcripts
        transcripts.sort(Comparator.comparing(SplicingTranscript::getAccessionId));

        // ---     FIRST      ---
        SplicingTranscript first = transcripts.get(0);
        assertThat(first.getAccessionId(), is("FIRST"));
        assertThat(first.getContig(), is("chr1"));
        assertThat(first.getTxBegin(), is(1000));
        assertThat(first.getTxEnd(), is(2000));
        assertThat(first.getStrand(), is(true));

        final List<SplicingExon> firstExons = first.getExons();
        assertThat(firstExons, hasSize(3));
        assertThat(firstExons, hasItems(
                SplicingExon.newBuilder().setBegin(1000).setEnd(1200).build(),
                SplicingExon.newBuilder().setBegin(1400).setEnd(1600).build(),
                SplicingExon.newBuilder().setBegin(1800).setEnd(2000).build()));

        final List<SplicingIntron> firstIntrons = first.getIntrons();
        assertThat(firstIntrons, hasSize(2));
        assertThat(firstIntrons, hasItems(
                SplicingIntron.newBuilder().setBegin(1200).setEnd(1400).setDonorScore(9.433).setAcceptorScore(7.392).build(),
                SplicingIntron.newBuilder().setBegin(1600).setEnd(1800).setDonorScore(4.931).setAcceptorScore(7.832).build()));

        // ---     SECOND     ---
        SplicingTranscript second = transcripts.get(1);
        assertThat(second.getAccessionId(), is("SECOND"));
        assertThat(second.getContig(), is("chr1"));
        assertThat(second.getTxBegin(), is(5000));
        assertThat(second.getTxEnd(), is(6000));
        assertThat(second.getStrand(), is(true));

        final List<SplicingExon> secondExons = second.getExons();
        assertThat(secondExons, hasSize(4));
        assertThat(secondExons, hasItems(
                SplicingExon.newBuilder().setBegin(5000).setEnd(5100).build(),
                SplicingExon.newBuilder().setBegin(5300).setEnd(5500).build(),
                SplicingExon.newBuilder().setBegin(5800).setEnd(5900).build(),
                SplicingExon.newBuilder().setBegin(5950).setEnd(6000).build()));

        final List<SplicingIntron> secondIntrons = second.getIntrons();
        assertThat(secondIntrons, hasSize(3));
        assertThat(secondIntrons, hasItems(
                SplicingIntron.newBuilder().setBegin(5100).setEnd(5300).setDonorScore(5.329).setAcceptorScore(3.848).build(),
                SplicingIntron.newBuilder().setBegin(5500).setEnd(5800).setDonorScore(9.740).setAcceptorScore(6.348).build(),
                SplicingIntron.newBuilder().setBegin(5900).setEnd(5950).setDonorScore(5.294).setAcceptorScore(8.239).build()));
    }
}