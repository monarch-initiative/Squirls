package org.monarchinitiative.squirls.ingest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * These tests serve to generate small databases for testing in user's home directory.
 */
@Disabled("This test is run only to generate small database for testing of other programs")
class FakeUpDatabase {

    private static final Path BUILD_DIR = Paths.get(System.getProperty("user.home"));

    private static final Path HG19_JANNOVAR_DB_DIR = Paths.get(FakeUpDatabase.class.getResource("transcripts/hg19").getPath());

    private static final Path HG38_JANNOVAR_DB_DIR = Paths.get(FakeUpDatabase.class.getResource("transcripts/hg38").getPath());

    private static final Path SPLICING_IC_MATRIX_PATH = Paths.get(FakeUpDatabase.class.getResource("spliceSites.yaml").getPath());
    private static final Path HEXAMER_TSV_PATH = Paths.get(FakeUpDatabase.class.getResource("hexamer-scores.tsv").getPath());
    private static final Path SEPTAMER_TSV_PATH = Paths.get(FakeUpDatabase.class.getResource("septamer-scores.tsv").getPath());

    private static final Map<String, Path> MODEL_PATHS = Map.of(
            "v0.4.1", Paths.get(FakeUpDatabase.class.getResource("example_model.v0.4.1.yaml").getPath()),
            "v1.1", Paths.get(FakeUpDatabase.class.getResource("example_model.v1.1.sklearn-0.23.1-slope-intercept-array.yaml").getPath()));

    private static final Map<String, byte[]> MODEL_DATA = new HashMap<>();

    @BeforeAll
    static void beforeAll() throws Exception {
        for (Map.Entry<String, Path> entry : MODEL_PATHS.entrySet()) {
            try (InputStream is = Files.newInputStream(entry.getValue())) {
                MODEL_DATA.put(entry.getKey(), is.readAllBytes());
            }
        }
    }

    @Test
    void makeHg19Database() throws Exception {
        URL genomeUrl = new URL("http://hgdownload.soe.ucsc.edu/goldenPath/hg19/bigZips/chromFa.tar.gz");
        SquirlsDataBuilder.buildDatabase(BUILD_DIR, genomeUrl, HG19_JANNOVAR_DB_DIR, SPLICING_IC_MATRIX_PATH,
                HEXAMER_TSV_PATH, SEPTAMER_TSV_PATH, MODEL_DATA, "1710_hg19");
    }

    @Test
    void makeHg38Database() throws Exception {
        URL genomeUrl = new URL("http://hgdownload.soe.ucsc.edu/goldenPath/hg38/bigZips/hg38.chromFa.tar.gz");
        SquirlsDataBuilder.buildDatabase(BUILD_DIR, genomeUrl, HG38_JANNOVAR_DB_DIR, SPLICING_IC_MATRIX_PATH,
                HEXAMER_TSV_PATH, SEPTAMER_TSV_PATH, MODEL_DATA, "1710_hg38");
    }
}
