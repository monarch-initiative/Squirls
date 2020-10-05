package org.monarchinitiative.squirls.ingest.conservation;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.ingest.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@SpringBootTest(classes = TestDataSourceConfig.class)
public class BigWigAccessorTest {

    /**
     * Small bigWig file containing phyloP scores for region chr9:100,000-101,000 (0-based).
     */
    private static final Path BW_PATH = Paths.get(TestDataSourceConfig.class.getResource("gck_hnf4a_fbn1.bw").getPath());

    @Autowired
    private ReferenceDictionary rd;

    private BigWigAccessor dao;

    @BeforeEach
    public void setUp() throws Exception {
        dao = new BigWigAccessor(BW_PATH);
    }

    @AfterEach
    public void tearDown() throws Exception {
        dao.close();
    }

    @Test
    public void getScores() throws Exception {
        float[] scores = dao.getScores("chr7", 44_182_371, 44_182_375);
        assertThat("Expected to find 4 elements", scores.length, is(4));
        assertThat(scores, is(new float[]{.349f, .364f, .349f, -.661f}));
    }

    @Test
    public void getScoresForInterval() throws Exception {
        // Get scores for FWD strand
        GenomeInterval interval = new GenomeInterval(rd, Strand.FWD, 7, 44_182_371, 44_182_375);
        float[] scores = dao.getScores(interval);
        assertThat(scores, is(new float[]{.349f, .364f, .349f, -.661f}));

        // Get scores for REV strand
        interval = new GenomeInterval(rd, Strand.FWD, 7, 44_182_371, 44_182_375).withStrand(Strand.REV);
        scores = dao.getScores(interval);
        assertThat(scores, is(new float[]{-.661f, .349f, .364f, .349f}));
    }

    @Test
    public void getScoresNotPresent() throws Exception {
        // score for the position 44_182_370 is not present in the file
        final float[] bla = dao.getScores("chr7", 44_182_369, 44_182_370);
        assertThat(bla[0], is(Float.NaN));
    }
}