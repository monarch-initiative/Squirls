package org.monarchinitiative.squirls.ingest.reference;

import htsjdk.samtools.reference.FastaSequenceIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.ingest.TestDataSourceConfig;
import org.monarchinitiative.squirls.ingest.TestUtils;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = TestDataSourceConfig.class)
class GenomeAssemblyDownloaderTest {

    private Path buildDir;


    @BeforeEach
    void setUp() throws Exception {
        buildDir = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")).resolve("3S-TEST"));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.deleteFolderAndFiles(buildDir);
    }


    @Test
    void download() {
        URL fastaUrl = TestDataSourceConfig.class.getResource("shortHg19ChromFa.tar.gz");


        Path whereToSave = buildDir.resolve("the-genome.fa");

        assertThat(whereToSave.toFile().exists(), is(false));
        GenomeAssemblyDownloader downloader = new GenomeAssemblyDownloader(fastaUrl, whereToSave, true);
        downloader.run();

        Path expectedFastaIdxPath = buildDir.resolve("the-genome.fa.fai");
        assertThat(expectedFastaIdxPath.toFile().isFile(), is(true));

        Path expectedFastaDictPath = buildDir.resolve("the-genome.fa.dict");
        assertThat(expectedFastaDictPath.toFile().isFile(), is(true));

        FastaSequenceIndex index = new FastaSequenceIndex(expectedFastaIdxPath);

        assertThat(index.size(), is(93));
    }
}