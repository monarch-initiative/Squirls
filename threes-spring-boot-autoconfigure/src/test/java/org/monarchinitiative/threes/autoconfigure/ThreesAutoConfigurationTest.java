package org.monarchinitiative.threes.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThreesAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    void testAllPropertiesSupplied() {
        load(ThreesAutoConfiguration.class, "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.transcript-source=refseq");
        Path threesDataDirectory = this.context.getBean("threesDataDirectory", Path.class);
        assertThat(threesDataDirectory.getFileName(), equalTo(Paths.get("data")));
    }

    @Test
    void testMissingDataDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Path to threes data directory (`--threes.data-directory`) is not specified"));
    }

    @Test
    void testMissingGenomeAssembly() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
//                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Genome assembly (`--threes.genome-assembly`) is not specified"));
    }

    @Test
    void testMissingDataVersion() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
//                "threes.data-version=1710",
                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Data version (`--threes.data-version`) is not specified"));
    }

    @Test
    void testMissingTranscriptSource() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710"));
//                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Transcript source (`--threes.transcript-source`) is not specified"));
    }
}