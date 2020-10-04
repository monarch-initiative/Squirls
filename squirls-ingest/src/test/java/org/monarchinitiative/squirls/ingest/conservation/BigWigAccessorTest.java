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
    private static final Path BW_PATH = Paths.get(BigWigAccessorTest.class.getResource("small.bw").getPath());

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
        float[] beginScores = dao.getScores("chr9", 100_000, 100_005);
        assertThat("Expected to find 5 elements", beginScores.length, is(5));
        assertThat(beginScores, is(new float[]{1.206F, 0.27F, 0.007F, 1.206F, 1.232F}));

        float[] endScores = dao.getScores("chr9", 100_995, 101_000);
        assertThat("Expected to find 5 elements", endScores.length, is(5));
        assertThat(endScores, is(new float[]{-0.557F, -0.952F, 0.747F, 1.958F, 0.706F}));
    }

    @Test
    public void getScoresForInterval() throws Exception {
        // Get scores for FWD strand
        GenomeInterval interval = new GenomeInterval(rd, Strand.FWD, 9, 100_000, 100_005);
        float[] scores = dao.getScores(interval);
        assertThat(scores, is(new float[]{1.206F, 0.27F, 0.007F, 1.206F, 1.232F}));

        // Get scores for REV strand
        interval = new GenomeInterval(rd, Strand.FWD, 9, 100_000, 100_005).withStrand(Strand.REV);
        scores = dao.getScores(interval);
        assertThat(scores, is(new float[]{1.232F, 1.206F, 0.007F, 0.27F, 1.206F}));
    }

    @Test
    public void getAllScores() throws Exception {
        float[] beginScores = dao.getScores("chr9", 100_000, 101_000);

        assertThat("Expected to find 1,000 elements", beginScores.length, is(1_000));
    }

    @Test
    public void getScoresNotPresent() throws Exception {
        // score for the position 99_999 is not present in the file
        final float[] bla = dao.getScores("chr9", 99_999, 100_005);
        assertThat(bla[0], is(Float.NaN));

        // again, score for the position 101_101 is not present in the file
        final float[] abl = dao.getScores("chr9", 100_995, 101_001);
        assertThat(abl[5], is(Float.NaN));
    }
}