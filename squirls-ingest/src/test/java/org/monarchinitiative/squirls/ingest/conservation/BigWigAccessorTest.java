package org.monarchinitiative.squirls.ingest.conservation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.ingest.TestDataSourceConfig;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(classes = TestDataSourceConfig.class)
class BigWigAccessorTest {

    /**
     * Small bigWig file containing phyloP scores for region chr9:100,000-101,000 (0-based).
     */
    private static final Path BW_PATH = Paths.get(BigWigAccessorTest.class.getResource("small.bw").getPath());

    private BigWigAccessor dao;

    @BeforeEach
    void setUp() throws Exception {
        dao = new BigWigAccessor(BW_PATH);
    }

    @AfterEach
    void tearDown() throws Exception {
        dao.close();
    }

    @Test
    void getScores() throws Exception {
        List<Float> beginScores = dao.getScores("chr9", 100_000, 100_005);

        assertThat("Expected to find 5 elements", beginScores, hasSize(5));
        assertThat(beginScores, hasItems(1.206F, 0.27F, 0.007F, 1.206F, 1.232F));

        final List<Float> endScores = dao.getScores("chr9", 100_995, 101_000);
        assertThat("Expected to find 5 elements", endScores, hasSize(5));
        assertThat(endScores, hasItems(-0.557F, -0.952F, 0.747F, 1.958F, 0.706F));
    }

    @Test
    void getAllScores() throws Exception {
        List<Float> beginScores = dao.getScores("chr9", 100_000, 101_000);

        assertThat("Expected to find 1,000 elements", beginScores, hasSize(1_000));
    }

    @Test
    void getScoresNotPresent() throws Exception {
        // score for the position 99_999 is not present in the file
        assertThrows(SquirlsWigException.class, () -> dao.getScores("chr9", 99_999, 100_005));

        // again, score for the position 101_101 is not present in the file
        assertThrows(SquirlsWigException.class, () -> dao.getScores("chr9", 100_995, 101_001));
    }
}