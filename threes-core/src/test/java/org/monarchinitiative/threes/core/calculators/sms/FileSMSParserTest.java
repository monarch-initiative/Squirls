package org.monarchinitiative.threes.core.calculators.sms;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.data.sms.FileSMSParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

class FileSMSParserTest {


    @Test
    void theFileIsParsed() throws Exception {
        final Path path = Paths.get(FileSMSParserTest.class.getResource("good-septamers.tsv").getPath());
        FileSMSParser parser = new FileSMSParser(path);
        final Map<String, Double> septamerMap = parser.getSeptamerMap();
        assertThat(septamerMap.size(), is(4096));
        assertThat(septamerMap, hasEntry("AAAAAAA", -0.0159));
    }

    @Test
    void corruptLinesAreIgnored() throws Exception {
        final Path path = Paths.get(FileSMSParserTest.class.getResource("invalid-septamers.tsv").getPath());
        FileSMSParser parser = new FileSMSParser(path);

        final Map<String, Double> septamerMap = parser.getSeptamerMap();

        assertThat(septamerMap.size(), is(1));
        assertThat(septamerMap, hasEntry("AAAAAAA", -0.0159));
    }
}