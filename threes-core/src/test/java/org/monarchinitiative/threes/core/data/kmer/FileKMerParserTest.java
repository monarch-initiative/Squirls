package org.monarchinitiative.threes.core.data.kmer;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

class FileKMerParserTest {


    @Test
    void theFileIsParsed() throws Exception {
        final Path path = Paths.get(FileKMerParserTest.class.getResource("good-septamers.tsv").getPath());
        FileKMerParser parser = new FileKMerParser(path);
        final Map<String, Double> septamerMap = parser.getKmerMap();
        assertThat(septamerMap.size(), is(4096));
        assertThat(septamerMap, hasEntry("AAAAAAA", -0.0159));
    }

    @Test
    void corruptLinesAreIgnored() throws Exception {
        final Path path = Paths.get(FileKMerParserTest.class.getResource("invalid-septamers.tsv").getPath());
        FileKMerParser parser = new FileKMerParser(path);

        final Map<String, Double> septamerMap = parser.getKmerMap();

        assertThat(septamerMap.size(), is(1));
        assertThat(septamerMap, hasEntry("AAAAAAA", -0.0159));
    }

    @Test
    void hexamersAreParsed() throws Exception {
        final Path path = Paths.get(FileKMerParserTest.class.getResource("hexamer-scores.tsv").getPath());
        FileKMerParser parser = new FileKMerParser(path);

        final Map<String, Double> kmerMap = parser.getKmerMap();

        assertThat(kmerMap.size(), is(2));
        assertThat(kmerMap, hasEntry("AAAAAT", -0.3426384));
        assertThat(kmerMap, hasEntry("TTTTTT", -0.3813581));

    }
}